package com.sadevolui.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private final JwtService service = new JwtService("this-is-a-test-secret-with-more-than-32-bytes", 60000);

    @Test
    void generatedTokenContainsTheUserIdentity() {
        UUID userId = UUID.randomUUID();
        String token = service.generateToken(userId, "user@example.com");
        assertThat(service.parseUserId(token)).isEqualTo(userId);
    }

    @Test
    void rejectsModifiedToken() {
        String token = service.generateToken(UUID.randomUUID(), "user@example.com");
        String modified = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");
        assertThatThrownBy(() -> service.parseUserId(modified)).isInstanceOf(RuntimeException.class);
    }
}
