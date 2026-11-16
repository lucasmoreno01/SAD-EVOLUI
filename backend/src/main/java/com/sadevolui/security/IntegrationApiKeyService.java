package com.sadevolui.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.sadevolui.shared.exception.UnauthorizedException;

@Service
public class IntegrationApiKeyService {
    private final String configuredApiKey;

    public IntegrationApiKeyService(@Value("${app.integration.api-key:}") String configuredApiKey) {
        this.configuredApiKey = configuredApiKey;
    }

    public void requireValid(String presentedApiKey) {
        if (configuredApiKey.isBlank() || presentedApiKey == null
                || !MessageDigest.isEqual(
                        configuredApiKey.getBytes(StandardCharsets.UTF_8),
                        presentedApiKey.getBytes(StandardCharsets.UTF_8))) {
            throw new UnauthorizedException("Chave de integração inválida.");
        }
    }
}
