package org.cc.ua;

import org.cc.common.utils.PlatformUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@SpringBootApplication
@ComponentScans(value = {
		@ComponentScan("org.cc.common.component"),
		@ComponentScan("org.cc.common.config")})
@EnableDiscoveryClient
public class UaApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext app = SpringApplication.run(UaApplication.class, args);
		PlatformUtil.log(app.getEnvironment());
	}
}
