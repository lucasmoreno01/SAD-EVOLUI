package com.sadevolui.modules.auth.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sadevolui.modules.auth.dto.AuthResponse;
import com.sadevolui.modules.auth.dto.LoginRequest;
import com.sadevolui.modules.auth.dto.RegisterRequest;
import com.sadevolui.modules.user.entity.UserAccount;
import com.sadevolui.modules.user.repository.UserRepository;
import com.sadevolui.security.JwtService;
import com.sadevolui.shared.exception.ConflictException;
import com.sadevolui.shared.exception.UnauthorizedException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) throw new ConflictException("Já existe uma conta para este e-mail.");
        UserAccount user;
        try {
            user = userRepository.save(new UserAccount(request.name(), email, passwordEncoder.encode(request.password())));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Já existe uma conta para este e-mail.");
        }
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .filter(account -> passwordEncoder.matches(request.password(), account.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("E-mail ou senha inválidos."));
        return response(user);
    }

    private AuthResponse response(UserAccount user) {
        return new AuthResponse(jwtService.generateToken(user.getId(), user.getEmail()), user.getId(), user.getName(), user.getEmail());
    }
}
