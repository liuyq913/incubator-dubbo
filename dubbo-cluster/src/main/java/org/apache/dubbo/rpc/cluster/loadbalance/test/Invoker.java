package org.apache.dubbo.rpc.cluster.loadbalance.test;

/**
 * Created by liuyq on 2019/2/25.
 */
public class Invoker {
    private String methodKey;

    private Integer weight;

    private String urlKey;

    private String name;

    public Invoker(String name, String methodKey, String urlKey, Integer weight) {
        this.name = name;
        this.methodKey = methodKey;
        this.urlKey = urlKey;
        this.weight = weight;
    }

    public String getMethodKey() {
        return methodKey;
    }

    public void setMethodKey(String methodKey) {
        this.methodKey = methodKey;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getUrlKey() {
        return urlKey;
    }

    public void setUrlKey(String urlKey) {
        this.urlKey = urlKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}