package com.tomek4861.cryptopositionmanager;

import com.tomek4861.cryptopositionmanager.entity.User;
import com.tomek4861.cryptopositionmanager.service.JwtService;
import com.tomek4861.cryptopositionmanager.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "app.oauth2.redirect-uri=http://localhost/cb"
})
@AutoConfigureMockMvc
class ApiExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private static final String AUTH = "Bearer good.token";

    @BeforeEach
    void authMocks() {
        // allow auth through security filter
        when(jwtService.extractUsername("good.token")).thenReturn("tester");
        User principal = new User();
        principal.setUsername("tester");
        principal.setPassword("pw");
        when(userDetailsService.loadUserByUsername("tester")).thenReturn(principal);
        when(jwtService.isTokenValid(anyString(), (UserDetails) org.mockito.ArgumentMatchers.any())).thenReturn(true);
    }

    // 1) body @Valid -> 400 + field messages (MethodArgumentNotValidException)
    @Test
    void errors_bodyValidation_returns400_withFieldMessages() throws Exception {
        // empty JSON -> missing required fields
        mvc.perform(post("/positions/preview")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                // error string should include field names
                .andExpect(jsonPath("$.error", containsString("ticker")))
                .andExpect(jsonPath("$.error", containsString("isLong")))
                .andExpect(jsonPath("$.error", containsString("entryPrice")))
                .andExpect(jsonPath("$.error", containsString("stopLoss")));
    }

    // 2) @RequestParam constraints -> 400 + details (HandlerMethodValidationException)
    @Test
    void errors_paramValidation_returns400_withDetails() throws Exception {
        // year=2019 (<2020) and month=13 (>12)
        mvc.perform(get("/stats/pnl")
                        .header("Authorization", AUTH)
                        .param("year", "1019")
                        .param("month", "13"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                // assert on default messages (robust to field name format)
                .andExpect(jsonPath("$.error", containsString("must be greater than or equal to 2020")))
                .andExpect(jsonPath("$.error", containsString("must be less than or equal to 12")));
    }
}
