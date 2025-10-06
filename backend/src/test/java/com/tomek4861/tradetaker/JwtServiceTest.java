package com.tomek4861.tradetaker;

import com.tomek4861.tradetaker.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    // helper: configure service with secret + expiration
    private JwtService createJwtService(long expMillis) {
        JwtService s = new JwtService();
        setField(s, "secretKey", JwtServiceTest.SECRET_B64);
        setField(s, "jwtExpiration", expMillis);
        return s;
    }

    // helper: set private field via reflection
    private static void setField(Object target, String name, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // stable 32-byte key -> Base64
    private static final String SECRET_B64 = Base64.getEncoder()
            .encodeToString("0123456789ABCDEF0123456789ABCDEF".getBytes(StandardCharsets.UTF_8));

    private static UserDetails user(String name) {
        return User.withUsername(name).password("pw").build();
    }

    // 1) jwt_generateAndExtractUsername_roundTrip_ok
    @Test
    void jwt_generateAndExtractUsername_roundTrip_ok() {
        JwtService jwt = createJwtService(3600_000); // 1h
        UserDetails u = user("alice");

        String token = jwt.generateToken(u);
        String subject = jwt.extractUsername(token);

        assertThat(subject).isEqualTo("alice");
    }

    // 2) jwt_isTokenValid_trueForSameUser_notExpired
    @Test
    void jwt_isTokenValid_trueForSameUser_notExpired() {
        JwtService jwt = createJwtService(3600_000);
        UserDetails u = user("bob");

        String token = jwt.generateToken(u);

        assertThat(jwt.isTokenValid(token, u)).isTrue();
    }

    // 3) jwt_isTokenValid_falseWhenExpired
    @Test
    void jwt_isTokenValid_falseWhenExpired() {
        // expired token (negative exp)
        JwtService jwtExpired = createJwtService(-1_000);
        UserDetails u = user("carol");
        String expired = jwtExpired.generateToken(u);

        // validate using same secret
        JwtService jwt = createJwtService(3600_000);

        boolean valid;
        try {
            valid = jwt.isTokenValid(expired, u);
        } catch (ExpiredJwtException e) {
            valid = false;
        }

        assertThat(valid).isFalse();
    }

    // 4) jwt_validateTokenQuickly_returnsFalseOnGarbage
    @Test
    void jwt_validateTokenQuickly_returnsFalseOnGarbage() {
        JwtService jwt = createJwtService(3600_000);

        assertThat(jwt.validateTokenQuickly("garbage.token.value")).isFalse();
        assertThat(jwt.validateTokenQuickly("")).isFalse();
        assertThat(jwt.validateTokenQuickly("abc")).isFalse();
    }
}
