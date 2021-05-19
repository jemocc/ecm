package org.cc.fileserver.controller;

import org.cc.common.model.RspResult;
import org.cc.common.server.TestRPCService;
import org.cc.common.utils.DateTimeUtil;
import org.cc.fileserver.Server.IMessageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class HelloCtrl {
    private final Logger log = LoggerFactory.getLogger(HelloCtrl.class);

    private final TestRPCService testRPCService;
    private final IMessageProvider iMessageProvider;

    public HelloCtrl(TestRPCService testRPCService, IMessageProvider iMessageProvider) {
        this.testRPCService = testRPCService;
        this.iMessageProvider = iMessageProvider;
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

    @GetMapping(value = "/pr/hello2")
    public RspResult<String> echo2(){
        log.info("测试中文字符");
        log.info(LocalDateTime.now().format(DateTimeUtil.DEFAULT_DATE_TIME_FORMATTER));
        return RspResult.ok("测试中文字符返回");
    }

    @GetMapping(value = "/pr/test-stream/{msg}")
    public RspResult<Void> testStream(@PathVariable("msg") String msg){
        iMessageProvider.sendTestMsg(msg);
        return RspResult.ok(null);
    }
}
