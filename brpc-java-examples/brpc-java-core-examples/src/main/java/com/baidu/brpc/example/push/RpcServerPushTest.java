/*
 * Copyright (c) 2018 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baidu.brpc.example.push;

import com.baidu.brpc.example.push.userservice.PushData;
import com.baidu.brpc.example.push.userservice.PushResult;
import com.baidu.brpc.example.push.userservice.ServerSideUserPushApi;
import com.baidu.brpc.example.standard.EchoServiceImpl;
import com.baidu.brpc.protocol.Options;
import com.baidu.brpc.server.BrpcPushProxy;
import com.baidu.brpc.server.RpcServer;
import com.baidu.brpc.server.RpcServerOptions;
import com.baidu.brpc.utils.GsonUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
@Slf4j
public class RpcServerPushTest {

    public static void main(String[] args) throws InterruptedException {

        //  org.apache.log4j.Logger.getLogger("com").setLevel(Level.ERROR);

        int port = 8002;
        if (args.length == 1) {
            port = Integer.valueOf(args[0]);
        }

        RpcServerOptions options = new RpcServerOptions();
        options.setReceiveBufferSize(64 * 1024 * 1024);
        options.setSendBufferSize(64 * 1024 * 1024);
        options.setKeepAliveTime(20);
        options.setProtocolType(Options.ProtocolType.PROTOCOL_SERVER_PUSH_VALUE);

        final RpcServer rpcServer = new RpcServer(port, options);

        rpcServer.registerService(new EchoServiceImpl());

        // get push api
        ServerSideUserPushApi proxyPushApi =
                (ServerSideUserPushApi) BrpcPushProxy.getProxy(rpcServer, ServerSideUserPushApi.class);
        rpcServer.start();

        Thread.sleep(15 * 1000);

        // push data to 2 clients : "c1" and "c2"
        int i = 0;
        while (true) {
            i++;
            PushData p = new PushData();
            p.setData("pushData" + i);
            String clientName = "c" + String.valueOf(i % 2 + 1);
            log.info("pushing data to client:" + clientName);
            try {
                // last param of api is clientName
                PushResult pushResult = proxyPushApi.clientReceive(clientName, p);
                log.info("received push result:" + GsonUtils.toJson(pushResult));
            } catch (Exception e) {
                log.error("push exception , please start up client c1 and c2", e);
            }

            Thread.sleep(5 * 1000);
        }

    }
}
