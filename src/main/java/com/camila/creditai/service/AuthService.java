package com.camila.creditai.service;

import com.camila.creditai.dto.request.LoginRequest;
import com.camila.creditai.dto.request.RegisterRequest;
import com.camila.creditai.dto.response.LoginResponse;
import com.camila.creditai.entity.User;
import com.camila.creditai.enums.Role;
import com.camila.creditai.repository.UserRepository;
import com.camila.creditai.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Usuário já existe");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return new LoginResponse(token, user.getUsername());
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return new LoginResponse(token, user.getUsername());
    }
}