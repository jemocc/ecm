package org.cc.fileserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "self-config")
@Setter
@Getter
public class SelfConfig {
    private String localFilePath;

}
