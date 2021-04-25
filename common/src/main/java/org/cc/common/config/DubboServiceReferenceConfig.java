package org.cc.common.config;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.cc.common.server.TestRPCService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: DubboServiceReferenceConfig
 * @Description: TODO
 * @Author: CC
 * @Date 2021/4/25 14:40
 * @ModifyRecords: v1.0 new
 */
@Configuration
public class DubboServiceReferenceConfig {

    @DubboReference(check = false, timeout = 1000, mock = "org.cc.common.server.mock.TestRPCServiceMock", methods = {
            @Method(name = "sayHello", timeout = 200)
    })
    private TestRPCService testRPCService;

    @Bean
    public TestRPCService testRPCService() {
        return testRPCService;
    }
}
