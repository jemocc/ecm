package org.cc.fileserver.controller;


import org.cc.common.model.RspResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloCtrl {
    private final Logger log = LoggerFactory.getLogger(HelloCtrl.class);

//    @DubboReference
//    private TestRPCService testRPCService;

    @GetMapping(value = "/api/hello")
    public RspResult<Void> hello(){
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        log.info("file-server authentication: {}", authentication.toString());
        return RspResult.ok(null);
    }

//    @GetMapping(value = "/pr/hello")
//    public RspResult<String> echo(){
//        String s = testRPCService.sayHello();
//        log.info("s: {}", s);
//        return RspResult.ok("hello");
//    }

}
