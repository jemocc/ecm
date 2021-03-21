package org.cc.ua.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class JdbcTemplateConfig {

    @Bean("JdbcTemplate")
    JdbcTemplate h2JdbcTemplate(DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }
}
