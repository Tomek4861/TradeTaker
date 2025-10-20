package com.tomek4861.tradetaker;

import com.tomek4861.tradetaker.entity.User;
import com.tomek4861.tradetaker.service.JwtService;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @TestConfiguration
    static class TestMocksConfig {
        @Bean
        public JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return Mockito.mock(UserDetailsService.class);
        }
    }

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    private static final String AUTH_HEADER = "Bearer good.token";

    @BeforeEach
    void authMocks() {
        // allow auth through security filter
        when(jwtService.extractUsername("good.token")).thenReturn("tester");
        User principal = new User();
        principal.setUsername("tester");
        principal.setPassword("pw");
        when(userDetailsService.loadUserByUsername("tester")).thenReturn(principal);
        when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(true);
    }

    // 1) body @Valid -> 400 + field messages
    @Test
    void errors_bodyValidation_returns400_withFieldMessages() throws Exception {
        // empty JSON -> missing required fields
        mvc.perform(post("/positions/preview")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error", containsString("ticker")))
                .andExpect(jsonPath("$.error", containsString("isLong")))
                .andExpect(jsonPath("$.error", containsString("entryPrice")))
                .andExpect(jsonPath("$.error", containsString("stopLoss")));
    }

    // 2) @RequestParam constraints -> 400 + details
    @Test
    void errors_paramValidation_returns400_withDetails() throws Exception {
        // year<2020 and month>12
        mvc.perform(get("/stats/pnl")
                        .header("Authorization", AUTH_HEADER)
                        .param("year", "1019")
                        .param("month", "13"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                // assert on default messages (robust to field name format)
                .andExpect(jsonPath("$.error", containsString("must be greater than or equal to 2020")))
                .andExpect(jsonPath("$.error", containsString("must be less than or equal to 12")));
    }


    // 3) String instead of Int in @RequestParam
    @Test
    void errors_paramValidationWrongType_returns400_withDetails() throws Exception {
        // checking global exception handler
        mvc.perform(
                        get("/stats/pnl")
                                .header("Authorization", AUTH_HEADER)
                                .param("year", "QWERTY")
                                .param("month", "12")

                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error", containsString("Invalid request parameters")));

    }

    // 4. Malformed Json in @RequestBody
    @Test
    void errors_bodyValidationMalformedJson_returns400() throws Exception {

        mvc.perform(
                        post("/positions/preview")
                                .header("Authorization", AUTH_HEADER)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                         {
                                         "ticker": "BTCUSDT", \s
                                        \s""")

                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error", containsString("Malformed JSON")));

    }
}
