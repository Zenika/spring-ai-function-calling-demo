package com.zenika.demo.ai.functioncalling;

import com.zenika.demo.ai.functioncalling.config.DossierRemboursementConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DossierRemboursementConfiguration.class)
public class AgenticAiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgenticAiDemoApplication.class, args);
    }

}
