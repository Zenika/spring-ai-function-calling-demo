package com.zenika.demo.ai.agentic.agenticaidemo;

import com.zenika.demo.ai.agentic.agenticaidemo.config.DossierRemboursementConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;

import java.util.Set;

@SpringBootApplication
@EnableConfigurationProperties(DossierRemboursementConfiguration.class)
@EnableCaching
public class AgenticAiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgenticAiDemoApplication.class, args);
    }


    @Bean
    CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Set.of(new ConcurrentMapCache("getSQL")));
        return cacheManager;
    }
}
