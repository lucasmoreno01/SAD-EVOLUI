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
        String[] parts = token.split("\\.");
        String signature = (parts[2].charAt(0) == 'A' ? "B" : "A") + parts[2].substring(1);
        String modified = parts[0] + "." + parts[1] + "." + signature;
        assertThatThrownBy(() -> service.parseUserId(modified)).isInstanceOf(RuntimeException.class);
    }
}
