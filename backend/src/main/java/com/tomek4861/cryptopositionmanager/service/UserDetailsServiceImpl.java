package com.tomek4861.cryptopositionmanager.service;

import com.tomek4861.cryptopositionmanager.entity.User;
import com.tomek4861.cryptopositionmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return user.get();
    }

    public User processOAuthPostLogin(String email, String username) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            User newUser = new User();
            String randomPassword = UUID.randomUUID().toString();
            newUser.setPassword(passwordEncoder.encode(randomPassword));
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setAuthProviderEnum(User.AuthProviderEnum.GOOGLE);
            userRepository.save(newUser);
            return newUser;
        }
        return user.get();

    }
}
