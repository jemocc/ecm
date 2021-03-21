package org.cc.ua.server;

import org.apache.dubbo.config.annotation.DubboService;
import org.cc.common.server.TestRPCService;

@DubboService
public class HelloService implements TestRPCService {

    @Override
    public String sayHello() {
        return "hello";
    }
}
