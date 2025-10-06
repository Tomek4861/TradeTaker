package com.tomek4861.tradetaker;

import com.tomek4861.tradetaker.config.JwtAuthenticationFilter;
import com.tomek4861.tradetaker.config.RestAuthenticationEntryPoint;
import com.tomek4861.tradetaker.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private HandlerExceptionResolver exceptionResolver;
    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private RestAuthenticationEntryPoint entryPoint;

    private OncePerRequestFilter filter;

    @BeforeEach
    void setUp() {
        exceptionResolver = mock(HandlerExceptionResolver.class);
        jwtService = mock(JwtService.class);
        userDetailsService = mock(UserDetailsService.class);
        entryPoint = mock(RestAuthenticationEntryPoint.class);

        filter = new JwtAuthenticationFilter(
                exceptionResolver,
                jwtService,
                userDetailsService,
                entryPoint
        );
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // simple chain that flips a flag when invoked
    static class StubChain implements FilterChain {
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
        StubChain chain = new StubChain();

        filter.doFilter(req, res, chain);

        assertThat(chain.called).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, userDetailsService, entryPoint);
    }

    // 2) Valid token -> sets SecurityContext and continues.
    @Test
    void filter_validToken_setsSecurityContext_andContinues() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer good.token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        StubChain chain = new StubChain();

        UserDetails u = User.withUsername("alice").password("pw").build();

        when(jwtService.extractUsername("good.token")).thenReturn("alice");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(u);
        when(jwtService.isTokenValid("good.token", u)).thenReturn(true);

        filter.doFilter(req, res, chain);

        assertThat(chain.called).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(u);
        verify(entryPoint, never()).commence(any(), any(), any());
    }

    // 3) Invalid/expired token -> entryPoint.commence, no chain.
    @Test
    void filter_invalidToken_callsEntryPoint_andStopsChain() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer bad.token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        StubChain chain = new StubChain();

        UserDetails u = User.withUsername("bob").password("pw").build();

        when(jwtService.extractUsername("bad.token")).thenReturn("bob");
        when(userDetailsService.loadUserByUsername("bob")).thenReturn(u);
        when(jwtService.isTokenValid("bad.token", u)).thenReturn(false);

        // entryPoint should be called with InsufficientAuthenticationException
        doNothing().when(entryPoint).commence(
                eq(req), eq(res), ArgumentMatchers.isA(InsufficientAuthenticationException.class)
        );

        filter.doFilter(req, res, chain);

        assertThat(chain.called).isFalse();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(entryPoint, times(1)).commence(eq(req), eq(res), isA(InsufficientAuthenticationException.class));
    }

    // 4) UserDetailsService throws UsernameNotFoundException -> entryPoint called.
    @Test
    void filter_usernameNotFound_callsEntryPoint() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        StubChain chain = new StubChain();

        when(jwtService.extractUsername("token")).thenReturn("ghost");
        when(userDetailsService.loadUserByUsername("ghost")).thenThrow(new UsernameNotFoundException("not found"));

        doNothing().when(entryPoint).commence(
                eq(req), eq(res), isA(InsufficientAuthenticationException.class)
        );

        filter.doFilter(req, res, chain);

        assertThat(chain.called).isFalse();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(entryPoint, times(1)).commence(eq(req), eq(res), isA(InsufficientAuthenticationException.class));
    }
}
