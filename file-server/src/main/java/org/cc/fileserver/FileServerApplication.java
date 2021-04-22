package org.cc.fileserver;

import org.cc.common.utils.PlatformUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@SpringBootApplication
@ComponentScans(value = {
		@ComponentScan("org.cc.common.component"),
		@ComponentScan("org.cc.common.config")})
@EnableDiscoveryClient
@EnableConfigurationProperties
public class FileServerApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext app = SpringApplication.run(FileServerApplication.class, args);
		PlatformUtil.log(app.getEnvironment());
	}

}
