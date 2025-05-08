package com.HandballStats_Pro.handballstatspro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long expiration;

    // public String getSecret() {
    //     return secret;
    // }

    // public void setSecret(String secret) {
    //     this.secret = secret;
    // }

    // public long getExpiration() {
    //     return expiration;
    // }

    // public void setExpiration(long expiration) {
    //     this.expiration = expiration;
    // }
}