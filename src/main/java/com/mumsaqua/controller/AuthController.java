package com.mumsaqua.controller;

import com.mumsaqua.entity.AppUser;
import com.mumsaqua.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        AppUser user = userRepository.findByUsername(auth.getName())
                .orElseThrow();
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername()
        ));
    }
}
