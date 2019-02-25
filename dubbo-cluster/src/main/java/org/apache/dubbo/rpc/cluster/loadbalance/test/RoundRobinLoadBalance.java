package org.apache.dubbo.rpc.cluster.loadbalance.test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by liuyq on 2019/2/25.
 */
public class RoundRobinLoadBalance {
    public static final String NAME = "roundrobin";

    private static int RECYCLE_PERIOD = 60000;

    protected static class WeightedRoundRobin {
        private int weight;
        private AtomicLong current = new AtomicLong(0);
        private long lastUpdate;

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
            current.set(0);
        }

        public long increaseCurrent() {
            return current.addAndGet(weight);
        }

        public void sel(int total) {
            current.addAndGet(-1 * total);
        }

        public long getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }

    private ConcurrentMap<String, ConcurrentMap<String, WeightedRoundRobin>> methodWeightMap = new ConcurrentHashMap<String, ConcurrentMap<String, WeightedRoundRobin>>();
    private AtomicBoolean updateLock      = new AtomicBoolean();

    public Invoker doSelect(List<Invoker> invokers) {
        String key = invokers.get(0).getMethodKey();
        ConcurrentMap<String, WeightedRoundRobin> map = methodWeightMap.get(key);
        if (map == null) {
            methodWeightMap.putIfAbsent(key, new ConcurrentHashMap<String, WeightedRoundRobin>());
            map = methodWeightMap.get(key);
        }
        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        long now = System.currentTimeMillis();
        Invoker selectedInvoker = null;
        WeightedRoundRobin selectedWRR = null;
        for (Invoker invoker : invokers) {
            String identifyString = invoker.getUrlKey();
            WeightedRoundRobin weightedRoundRobin = map.get(identifyString);
            int weight = invoker.getWeight();
            if (weight < 0) {
                weight = 0;
            }
            if (weightedRoundRobin == null) {
                weightedRoundRobin = new WeightedRoundRobin();
                weightedRoundRobin.setWeight(weight);
                map.putIfAbsent(identifyString, weightedRoundRobin);
                weightedRoundRobin = map.get(identifyString);
            }
            if (weight != weightedRoundRobin.getWeight()) {
                //weight changed
                weightedRoundRobin.setWeight(weight);
            }
            long cur = weightedRoundRobin.increaseCurrent();
            weightedRoundRobin.setLastUpdate(now);
            if (cur > maxCurrent) {
                maxCurrent = cur;
                selectedInvoker = invoker;
                selectedWRR = weightedRoundRobin;
            }
            totalWeight += weight;
        }
        if (!updateLock.get() && invokers.size() != map.size()) {
            if (updateLock.compareAndSet(false, true)) {
                try {
                    // copy -> modify -> update reference
                    ConcurrentMap<String, WeightedRoundRobin> newMap = new ConcurrentHashMap<String, WeightedRoundRobin>();
                    newMap.putAll(map);
                    Iterator<Map.Entry<String, WeightedRoundRobin>> it = newMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, WeightedRoundRobin> item = it.next();
                        if (now - item.getValue().getLastUpdate() > RECYCLE_PERIOD) {
                            it.remove();
                        }
                    }
                    methodWeightMap.put(key, newMap);
                } finally {
                    updateLock.set(false);
                }
            }
        }
        if (selectedInvoker != null) {
            selectedWRR.sel(totalWeight);
            return selectedInvoker;
        }
        // should not happen here
        return invokers.get(0);
    }

    public static void main(String[] args) {
        List<Invoker> invokers = new LinkedList<>();
        Invoker invoker = new Invoker("A", "dubbo://10.121.135.25:20880/DemoService/sayHello", "dubbo://10.121.135.25:20880/DemoService", 1);
        Invoker invoker2 = new Invoker("B", "dubbo://10.121.135.26:20880/DemoService/sayHello", "dubbo://10.121.135.26:20880/DemoService", 3);
        Invoker invoker3 = new Invoker("C", "dubbo://10.121.135.27:20880/DemoService/sayHello", "dubbo://10.121.135.27:20880/DemoService", 5);
        invokers.add(invoker);
        invokers.add(invoker2);
        invokers.add(invoker3);

        RoundRobinLoadBalance balance = new RoundRobinLoadBalance();
        for (int i = 0; i < 10; i++) {
            Invoker selectedInvoker = balance.doSelect(invokers);
            System.out.println(selectedInvoker.getName() + "  方法权重:" + selectedInvoker.getWeight());
        }

    }
}