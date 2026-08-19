package com.example.auth.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * TP4 - Configuration de la Master Key.
 * L'application refuse de démarrer si APP_MASTER_KEY est absente.
 */
@Configuration
public class MasterKeyConfig {

    private static final Logger logger = LoggerFactory.getLogger(MasterKeyConfig.class);

    @Value("${app.master-key:}")
    private String masterKey;

    @PostConstruct
    public void validateMasterKey() {
        if (masterKey == null || masterKey.isEmpty()) {
            logger.error("❌ APP_MASTER_KEY non définie ! L'application ne peut pas démarrer.");
            throw new IllegalStateException(
                    "APP_MASTER_KEY est obligatoire. Définissez-la en variable d'environnement."
            );
        }
        if (masterKey.length() < 32) {
            logger.warn("⚠️ APP_MASTER_KEY fait moins de 32 caractères (recommandé : 256 bits)");
        }
        logger.info("✅ Master Key configurée (longueur : {} caractères)", masterKey.length());
    }

    public String getMasterKey() {
        return masterKey;
    }
}