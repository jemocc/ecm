package org.cc.fileserver.controller;


import org.cc.common.model.RspResult;
import org.cc.common.server.TestRPCService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloCtrl {
    private final Logger log = LoggerFactory.getLogger(HelloCtrl.class);

    private final TestRPCService testRPCService;

    public HelloCtrl(TestRPCService testRPCService) {
        this.testRPCService = testRPCService;
    }

    @GetMapping(value = "/api/hello")
    public RspResult<Void> hello(){
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        log.info("file-server authentication: {}", authentication.toString());
        return RspResult.ok(null);
    }

    @GetMapping(value = "/pr/hello")
    public RspResult<String> echo(){
        StopWatch sw = new StopWatch();
        sw.start("test");
        String s = testRPCService.sayHello();
        sw.stop();
        log.info(sw.prettyPrint());
        return RspResult.ok(s);
    }

}
