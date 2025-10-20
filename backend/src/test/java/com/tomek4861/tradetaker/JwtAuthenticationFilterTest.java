package com.tomek4861.tradetaker;

import com.tomek4861.tradetaker.config.JwtAuthenticationFilter;
import com.tomek4861.tradetaker.config.RestAuthenticationEntryPoint;
import com.tomek4861.tradetaker.entity.User;
import com.tomek4861.tradetaker.service.JwtService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class JwtAuthenticationFilterTest {

    @TestConfiguration
    static class TestMocksConfig {

        @Bean
        @Primary
        public HandlerExceptionResolver mockHandlerExceptionResolver() {
            return Mockito.mock(HandlerExceptionResolver.class);
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
        public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
            return Mockito.mock(RestAuthenticationEntryPoint.class);
        }

        @Bean
        @Primary
        public OncePerRequestFilter oncePerRequestFilter(
                HandlerExceptionResolver handlerExceptionResolver,
                JwtService jwtService,
                UserDetailsService userDetailsService,
                RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
            return new JwtAuthenticationFilter(handlerExceptionResolver, jwtService, userDetailsService, restAuthenticationEntryPoint);
        }
    }

    @Autowired
    private HandlerExceptionResolver exceptionResolver;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private RestAuthenticationEntryPoint entryPoint;
    @Autowired
    private OncePerRequestFilter filter;


    @BeforeEach
    void setUp() {
        Mockito.reset(exceptionResolver, jwtService, userDetailsService, entryPoint);
        SecurityContextHolder.clearContext();
    }

    // flips a flag when invoked
    static class TestFilterChain implements FilterChain {
        boolean called = false;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            called = true;
        }
    }

    // 1) No Authorization header -> continue, no auth set.
    @Test
    void filter_noAuthHeader_continuesChain_noAuthSet() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        TestFilterChain chain = new TestFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(chain.called).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        // we do not call these classes cause no auth header
        verifyNoInteractions(jwtService, userDetailsService, entryPoint);
    }

    // 2) Valid token -> sets SecurityContext and continues.
    @Test
    void filter_validToken_setsSecurityContext_andContinues() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer good.token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        TestFilterChain chain = new TestFilterChain();

        User principal = new User();
        principal.setUsername("alice");
        principal.setPassword("pw");

        when(jwtService.extractUsername("good.token")).thenReturn("alice");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(principal);
        when(jwtService.isTokenValid("good.token", principal)).thenReturn(true);

        filter.doFilter(req, res, chain);

        assertThat(chain.called).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(principal);
        verifyNoInteractions(entryPoint);
    }

    // 3) Invalid/expired token -> entryPoint.commence, no chain.
    @Test
    void filter_invalidToken_callsEntryPoint_andStopsChain() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer bad.token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        TestFilterChain chain = new TestFilterChain();

        User principal = new User();
        principal.setUsername("bob");
        principal.setPassword("pw");

        when(jwtService.extractUsername("bad.token")).thenReturn("bob");
        when(userDetailsService.loadUserByUsername("bob")).thenReturn(principal);
        when(jwtService.isTokenValid("bad.token", principal)).thenReturn(false);

        filter.doFilter(req, res, chain);

        assertThat(chain.called).isFalse(); // we do not want to process this req further
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        // entryPoint should be called with InsufficientAuthenticationException
        verify(entryPoint).commence(eq(req), eq(res), isA(InsufficientAuthenticationException.class));
    }

    // 4) UserDetailsService throws UsernameNotFoundException -> entryPoint called.
    @Test
    void filter_usernameNotFound_callsEntryPoint() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        TestFilterChain chain = new TestFilterChain();

        when(jwtService.extractUsername("token")).thenReturn("ghost");
        when(userDetailsService.loadUserByUsername("ghost")).thenThrow(new UsernameNotFoundException("not found"));

        filter.doFilter(req, res, chain);

        assertThat(chain.called).isFalse();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(entryPoint).commence(eq(req), eq(res), isA(InsufficientAuthenticationException.class));
    }

    // 5) Malformed JWT token -> entryPoint called, no chain.
    @Test
    void filter_invalidMalformedToken_callsEntryPoint() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer ASDFGHJKL");
        MockHttpServletResponse res = new MockHttpServletResponse();
        TestFilterChain chain = new TestFilterChain();

        when(jwtService.extractUsername("ASDFGHJKL")).thenThrow(new MalformedJwtException(""));

        filter.doFilter(req, res, chain);

        assertThat(chain.called).isFalse();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(entryPoint).commence(eq(req), eq(res), isA(InsufficientAuthenticationException.class));

    }
}
