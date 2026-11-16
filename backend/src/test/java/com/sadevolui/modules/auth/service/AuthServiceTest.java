package com.sadevolui.modules.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import com.sadevolui.modules.auth.dto.LoginRequest;
import com.sadevolui.modules.auth.dto.RegisterRequest;
import com.sadevolui.modules.user.entity.UserAccount;
import com.sadevolui.modules.user.repository.UserRepository;
import com.sadevolui.security.JwtService;
import com.sadevolui.shared.exception.UnauthorizedException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtService jwtService = new JwtService("this-is-a-test-secret-with-more-than-32-bytes", 60000);
    private AuthService service;

    @BeforeEach
    void setUp() { service = new AuthService(userRepository, encoder, jwtService); }

    @Test
    void registrationHashesPasswordAndReturnsUsableJwt() {
        UUID id = UUID.randomUUID();
        AtomicReference<UserAccount> savedAccount = new AtomicReference<>();
        when(userRepository.existsByEmail("person@example.com")).thenReturn(false);
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount account = invocation.getArgument(0);
            ReflectionTestUtils.setField(account, "id", id);
            savedAccount.set(account);
            return account;
        });

        var response = service.register(new RegisterRequest("Pessoa", "Person@Example.com", "senha-segura-123"));

        assertThat(response.userId()).isEqualTo(id);
        assertThat(response.email()).isEqualTo("person@example.com");
        assertThat(encoder.matches("senha-segura-123", savedAccount.get().getPasswordHash())).isTrue();
        assertThat(jwtService.parseUserId(response.token())).isEqualTo(id);
    }

    @Test
    void loginRejectsWrongPassword() {
        UUID id = UUID.randomUUID();
        UserAccount account = new UserAccount("Pessoa", "person@example.com", encoder.encode("senha-correta-123"));
        ReflectionTestUtils.setField(account, "id", id);
        when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.login(new LoginRequest("person@example.com", "senha-errada-123")))
                .isInstanceOf(UnauthorizedException.class);
    }
}
