package com.tomek4861.cryptopositionmanager.controllers;


import com.tomek4861.cryptopositionmanager.dto.login.LoginRequest;
import com.tomek4861.cryptopositionmanager.dto.other.StandardResponse;
import com.tomek4861.cryptopositionmanager.dto.register.RegisterRequest;
import com.tomek4861.cryptopositionmanager.entity.User;
import com.tomek4861.cryptopositionmanager.service.AuthenticationService;
import com.tomek4861.cryptopositionmanager.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authService;

    @PostMapping("/login")
    public ResponseEntity<StandardResponse<String>> login(@Valid @RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest);
        return ResponseEntity.ok(StandardResponse.success(token));
    }


    @PostMapping("/register")
    public ResponseEntity<StandardResponse<String>> register(@Valid @RequestBody RegisterRequest registerRequest) {

        User newUser = authService.register(registerRequest);
        String token = jwtService.generateToken(newUser);

        return ResponseEntity.ok(StandardResponse.success(token));
    }

    @GetMapping("/status")
    public ResponseEntity<String> isAuthenticated(Principal principal) {

        String username = principal.getName();
        return ResponseEntity.ok(username);
    }


}
