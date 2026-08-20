package com.example.auth.controller;

import com.example.auth.dto.LoginHmacRequest;
import com.example.auth.entity.User;
import com.example.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestParam String email,
            @RequestParam String password) {
        authService.register(email, password);
        return ResponseEntity.ok(Map.of("message", "Inscription reussie."));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestParam String email,
            @RequestParam String password) {
        String token = authService.login(email, password);
        return ResponseEntity.ok(Map.of(
                "message", "Connexion reussie.",
                "token", token
        ));
    }

    @PostMapping("/api/auth/login-hmac")
    public ResponseEntity<Map<String, Object>> loginHmac(
            @RequestBody LoginHmacRequest request) {
        String token = authService.loginHmac(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Connexion reussie.",
                "token", token,
                "expiresAt", System.currentTimeMillis() + 900000
        ));
    }

    @GetMapping("/api/me")
    public ResponseEntity<Map<String, Object>> getMe(
            @RequestHeader("X-Session-Token") String token) {
        User user = authService.getMe(token);
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "createdAt", user.getCreatedAt().toString()
        ));
    }
}
