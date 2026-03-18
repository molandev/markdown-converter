package com.molandev.converter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 转换器配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "converter")
public class ConverterConfig {
    
    /**
     * 工具类型配置
     */
    private ToolConfig tool = new ToolConfig();
    
    /**
     * 并发控制配置
     */
    private ConcurrencyConfig concurrency = new ConcurrencyConfig();
    
    /**
     * 临时文件清理配置
     */
    private CleanupConfig cleanup = new CleanupConfig();
    
    @Data
    public static class ToolConfig {
        /**
         * 工具类型：all(全部), libreoffice, mineru, markitdown
         */
        private String type = "all";
    }
    
    @Data
    public static class ConcurrencyConfig {
        /**
         * 最大并发处理数量
         */
        private int maxConcurrent = 10;
        
        /**
         * 等待超时时间（秒）
         */
        private int acquireTimeout = 30;
    }
    
    @Data
    public static class CleanupConfig {
        /**
         * 临时文件保留时间（分钟）
         */
        private int retentionMinutes = 10;
        
        /**
         * 清理任务执行间隔（分钟）
         */
        private int scheduleMinutes = 5;
        
        /**
         * 临时文件基础目录
         * 默认为 /app/temp，可通过环境变量 CONVERTER_TEMP_DIR 覆盖
         */
        private String tempDir = "/app/temp";
    }
}
