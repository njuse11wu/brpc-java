package com.baidu.brpc.example.http.json;

import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcCallback;
import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.client.RpcClientOptions;
import com.baidu.brpc.client.loadbalance.LoadBalanceType;
import com.baidu.brpc.example.interceptor.CustomInterceptor;
import com.baidu.brpc.exceptions.RpcException;
import com.baidu.brpc.interceptor.Interceptor;
import com.baidu.brpc.protocol.Options.ProtocolType;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class RpcClientTest {
    public static void main(String[] args) {
        RpcClientOptions clientOption = new RpcClientOptions();
        clientOption.setHttp(true);
        clientOption.setProtocolType(ProtocolType.PROTOCOL_HTTP_JSON_VALUE);
        clientOption.setWriteTimeoutMillis(1000);
        clientOption.setReadTimeoutMillis(5000);
        clientOption.setLoadBalanceType(LoadBalanceType.WEIGHT.getId());
        clientOption.setMaxTryTimes(1);

        String serviceUrl = "list://127.0.0.1:8080";
        if (args.length == 1) {
            serviceUrl = args[0];
        }

        List<Interceptor> interceptors = new ArrayList<Interceptor>();;
        interceptors.add(new CustomInterceptor());
        RpcClient rpcClient = new RpcClient(serviceUrl, clientOption, interceptors);


        // sync call
        EchoService echoService = BrpcProxy.getProxy(rpcClient, EchoService.class);
        try {
            String response = echoService.hello("okok");
            System.out.printf("sync call success, response=%s\n", response);
        } catch (RpcException ex) {
            System.out.println("sync call failed, msg=" + ex.getMessage());
        }

        // async call
        RpcCallback callback = new RpcCallback<String>() {
            @Override
            public void success(String response) {
                if (response != null) {
                    System.out.printf("async call success, response=%s\n",
                            new Gson().toJson(response));
                } else {
                    System.out.println("async call failed");
                }
            }

            @Override
            public void fail(Throwable e) {
                System.out.printf("async call failed, %s\n", e.getMessage());
            }
        };
        EchoServiceAsync echoServiceAsync = BrpcProxy.getProxy(rpcClient, EchoServiceAsync.class);
        try {
            Future<String> future = echoServiceAsync.hello("ok", callback);
            try {
                if (future != null) {
                    future.get();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (RpcException ex) {
            System.out.println("send exception, ex=" + ex.getMessage());
        }
        rpcClient.stop();
    }
}