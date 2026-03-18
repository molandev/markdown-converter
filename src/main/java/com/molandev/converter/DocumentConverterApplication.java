package com.molandev.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class DocumentConverterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentConverterApplication.class, args);
    }

    @Component
    static class StartupMessagePrinter implements ApplicationRunner {
        
        @Override
        public void run(ApplicationArguments args) throws Exception {
            int port = Integer.parseInt(System.getProperty("server.port", "10996"));
            
            log.info("");
            log.info("========================================");
            log.info("  Document Converter Service Started");
            log.info("========================================");
            log.info("");
            log.info("🚀 Service is running at:");
            log.info("   http://localhost:{}", port);
            log.info("");
            log.info("📚 For API documentation, please visit:");
            log.info("   http://localhost:{}/help", port);
            log.info("");
            log.info("========================================");
        }
    }
}
