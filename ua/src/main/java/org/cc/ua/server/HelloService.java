package org.cc.ua.server;

import org.apache.dubbo.config.annotation.DubboService;
import org.cc.common.server.TestRPCService;
import org.cc.common.utils.PublicUtil;

@DubboService
public class HelloService implements TestRPCService {

    @Override
    public String sayHello() {
        PublicUtil.sleep(2000);
        return "hello";
    }
}
