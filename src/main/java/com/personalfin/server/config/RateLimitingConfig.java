package com.personalfin.server.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitingConfig {

    private int authRequestsPerMinute = 5;
    private int apiRequestsPerMinute = 100;
    private int burstCapacity = 10;

    @Bean
    public Cache<String, Bucket> rateLimitCache() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(1))
                .build();
    }

    public Bucket resolveBucket(String key, boolean isAuthEndpoint) {
        return rateLimitCache().get(key, k -> {
            int requestsPerMinute = isAuthEndpoint ? authRequestsPerMinute : apiRequestsPerMinute;
            Refill refill = Refill.intervally(requestsPerMinute, Duration.ofMinutes(1));
            Bandwidth limit = Bandwidth.classic(requestsPerMinute + burstCapacity, refill);
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        });
    }

    public int getAuthRequestsPerMinute() {
        return authRequestsPerMinute;
    }

    public void setAuthRequestsPerMinute(int authRequestsPerMinute) {
        this.authRequestsPerMinute = authRequestsPerMinute;
    }

    public int getApiRequestsPerMinute() {
        return apiRequestsPerMinute;
    }

    public void setApiRequestsPerMinute(int apiRequestsPerMinute) {
        this.apiRequestsPerMinute = apiRequestsPerMinute;
    }

    public int getBurstCapacity() {
        return burstCapacity;
    }

    public void setBurstCapacity(int burstCapacity) {
        this.burstCapacity = burstCapacity;
    }
}


