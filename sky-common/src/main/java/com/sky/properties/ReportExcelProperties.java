package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.excel")
@Data
public class ReportExcelProperties {
    private String filePath;
    private String[] sheet;

}