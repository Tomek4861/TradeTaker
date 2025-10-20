package com.tomek4861.tradetaker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomek4861.tradetaker.dto.login.LoginRequest;
import com.tomek4861.tradetaker.dto.positions.current.CurrentOpenPositionsResponse;
import com.tomek4861.tradetaker.dto.register.RegisterRequest;
import com.tomek4861.tradetaker.entity.User;
import com.tomek4861.tradetaker.service.AuthenticationService;
import com.tomek4861.tradetaker.service.JwtService;
import com.tomek4861.tradetaker.service.TradingOrchestrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest()
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class Configuration {

        @Bean
        public AuthenticationService authenticationService() {
            return Mockito.mock(AuthenticationService.class);
        }

        @Bean
        public JwtService jwtService() {
            return Mockito.mock(JwtService.class);

        }

        @Bean
        public UserDetailsService userDetailsService() {
            return Mockito.mock(UserDetailsService.class);
        }

        @Bean
        public TradingOrchestrationService tradingOrchestrationService() {
            return Mockito.mock(TradingOrchestrationService.class);
        }
    }

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TradingOrchestrationService tradingOrchestrationService;

    private static String toJson(Object o, ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setupMocks() {
        // generic default for /positions/open
        when(tradingOrchestrationService.getOpenPositionForUser(any(User.class)))
                .thenReturn(new CurrentOpenPositionsResponse());
    }

    // 1) security_publicEndpoints_permitAll
    @Test
    void security_publicEndpoints_permitAll() throws Exception {
        // /auth/login -> 200 with mocked service
        var login = new LoginRequest();
        login.setUsername("john");
        login.setPassword("pw");
        when(authenticationService.login(any())).thenReturn("jwt-login-token");

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(login, objectMapper)))
                .andExpect(status().isOk());

        // /auth/register -> 200 with mocked service
        var reg = RegisterRequest.builder()
                .username("john")
                .email("john@ex.com")
                .password("pw")
                .build();

        User saved = new User();
        saved.setUsername("john");
        when(authenticationService.register(any())).thenReturn(saved);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-reg-token");

        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(reg, objectMapper)))
                .andExpect(status().isOk());

        // /oauth2/-> should not be 401/403
        var res = mvc.perform(get("/oauth2/authorization/google"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(res).isNotIn(401, 403);
    }

    // 2) protected without JWT -> 401 + standard body
    @Test
    void security_protectedEndpoint_withoutJWT_returns401_withStandardBody() throws Exception {
        mvc.perform(get("/positions/open"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    // 3) protected with valid JWT -> 200
    @Test
    void security_protectedEndpoint_withValidJWT_returns200() throws Exception {
        // fake jwt flow
        when(jwtService.extractUsername("good.token")).thenReturn("alice");
        User principal = new User();
        principal.setUsername("alice");
        principal.setPassword("pw");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(principal);
        when(jwtService.isTokenValid("good.token", principal)).thenReturn(true);

        mvc.perform(get("/positions/open")
                        .header("Authorization", "Bearer good.token"))
                .andExpect(status().isOk());
    }

    // 4) security_protectedEndpoint_withJWT_userNotFound_returns401_withStandardBody
    @Test
    void security_protectedEndpoint_withJWT_userNotFound_returns401_withStandardBody() throws Exception {
        when(jwtService.extractUsername(anyString())).thenReturn("ghost");
        when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(true);
        when(userDetailsService.loadUserByUsername(anyString())).thenThrow(new UsernameNotFoundException(""));

        User principal = new User();
        principal.setUsername("alice");
        principal.setPassword("pw");
        String token = jwtService.generateToken(principal);

        mvc.perform(get("/positions/open")
                        .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error", containsString("Authentication required")))
        ;


    }


}
