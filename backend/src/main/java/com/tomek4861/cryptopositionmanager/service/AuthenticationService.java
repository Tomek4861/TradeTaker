package com.tomek4861.cryptopositionmanager.service;

import com.tomek4861.cryptopositionmanager.dto.login.LoginRequest;
import com.tomek4861.cryptopositionmanager.dto.register.RegisterRequest;
import com.tomek4861.cryptopositionmanager.entity.User;
import com.tomek4861.cryptopositionmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;


    public User register(RegisterRequest request) {
        User newUser = new User();
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEmail(request.getEmail());
        newUser.setUsername(request.getUsername());
        newUser.setAuthProviderEnum(User.AuthProviderEnum.LOCAL);

        userRepository.save(newUser);

        return newUser;
    }

    public String login(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByUsername(request.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );
        return jwtService.generateToken(user);
    }


}
