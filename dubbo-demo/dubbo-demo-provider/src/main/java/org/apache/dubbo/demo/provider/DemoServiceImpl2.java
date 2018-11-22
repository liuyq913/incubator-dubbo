package org.apache.dubbo.demo.provider;

import org.apache.dubbo.demo.DemoService2;
import org.apache.dubbo.rpc.RpcContext;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liuyq on 2018/11/22.  测试多个服务同事暴露  会不会调用createService（）两次
 */
public class DemoServiceImpl2 implements DemoService2 {
    @Override
    public String sayHello2(String name) {
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] Hello " + name + ", request from consumer: " + RpcContext.getContext().getRemoteAddress());
        return "Hello2 " + name + ", response from provider: " + RpcContext.getContext().getLocalAddress();
    }
}
