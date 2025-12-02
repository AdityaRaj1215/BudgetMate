package com.personalfin.server.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "your-secret-key-change-this-in-production-minimum-256-bits";
    private long expiration = 86400000; // 24 hours in milliseconds
    private String issuer = "personal-finance-server";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}






