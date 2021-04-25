package org.cc.common.server.mock;

import org.cc.common.server.TestRPCService;

/**
 * @ClassName: TestRPCServiceMock
 * @Description: TODO
 * @Author: CC
 * @Date 2021/4/25 15:11
 * @ModifyRecords: v1.0 new
 */
public class TestRPCServiceMock implements TestRPCService {
    @Override
    public String sayHello() {
        return "mock hello";
    }
}
