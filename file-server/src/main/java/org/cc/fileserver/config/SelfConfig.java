package org.cc.fileserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "self-config")
public class SelfConfig {
    private String localFilePath;

}
