package com.tomek4861.cryptopositionmanager.controllers;


import com.tomek4861.cryptopositionmanager.dto.login.LoginRequest;
import com.tomek4861.cryptopositionmanager.dto.login.LoginResponse;
import com.tomek4861.cryptopositionmanager.dto.register.RegisterRequest;
import com.tomek4861.cryptopositionmanager.dto.register.RegisterResponse;
import com.tomek4861.cryptopositionmanager.entity.User;
import com.tomek4861.cryptopositionmanager.service.AuthenticationService;
import com.tomek4861.cryptopositionmanager.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest);
        var response = new LoginResponse(true, token);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {

        User newUser = authService.register(registerRequest);
        String token = jwtService.generateToken(newUser);

        var response = new RegisterResponse(true, token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<String> isAuthenticated(Authentication authentication) {

        String username = authentication.getName();
        return ResponseEntity.ok(username);
    }


}
