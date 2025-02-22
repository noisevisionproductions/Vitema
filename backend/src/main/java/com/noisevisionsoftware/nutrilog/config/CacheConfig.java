package com.noisevisionsoftware.nutrilog.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.noisevisionsoftware.nutrilog.security.model.UserRole;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "dietsCache",
                "dietsListCache",
                "roleCache",
                "userEmailCache",
                "userRoles"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(10_000));
        return cacheManager;
    }

    @Bean
    public Cache<String, UserRole> roleCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    @Bean
    public Cache<String, String> userEmailCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }
}