package org.cc.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
public class GatewayApplication {
	private static final Logger log = LoggerFactory.getLogger(GatewayApplication.class);

	public static void main(String[] args) {
		ApplicationContext app = SpringApplication.run(GatewayApplication.class, args);
		log(app.getEnvironment());
	}

	private static void log(Environment env) {
		try {
			log.info("\n" +
					"----------------------------------------------------------\n" +
					"\tApplication '{}' is running! Access URLs:\n" +
					"\t\t     Local: http://localhost:{}\n" +
					"\t\t  External: http://{}:{}\n" +
					"\t\tProfile(s): {}\n" +
					"----------------------------------------------------------",
					env.getProperty("spring.application.name"),
					env.getProperty("server.port"),
					InetAddress.getLocalHost().getHostAddress(),
					env.getProperty("server.port"),
					env.getActiveProfiles());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
