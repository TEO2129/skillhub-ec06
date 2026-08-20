package com.example.auth.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MasterKeyConfig {

    private static final Logger logger = LoggerFactory.getLogger(MasterKeyConfig.class);

    @Value("${app.master-key:}")
    private String masterKey;

    @PostConstruct
    public void validateMasterKey() {
        if (masterKey == null || masterKey.isEmpty()) {
            logger.error("APP_MASTER_KEY non definie !");
            throw new IllegalStateException("APP_MASTER_KEY est obligatoire.");
        }
        logger.info("Master Key configuree (longueur : {})", masterKey.length());
    }

    public String getMasterKey() {
        return masterKey;
    }
}