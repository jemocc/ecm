package org.cc.gateway.config;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class ErrorPageConfig {
    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer(){
        return factory -> {
            ErrorPage page1 = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");
            ErrorPage page2 = new ErrorPage(HttpStatus.NOT_FOUND, "/error.html");
            factory.addErrorPages(page1, page2);
        };
    }
}
