package com.pragma.powerup.infrastructure.out.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-secret-key-with-at-least-32-characters";
    private static final String OTHER_SECRET = "another-secret-key-with-at-least-32-chars";

    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET);
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final FilterChain filterChain = mock(FilterChain.class);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private String token(String secret, long expirationOffsetMs) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
                .subject("owner@gmail.com")
                .claim("userId", 7L)
                .claim("role", "OWNER")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationOffsetMs))
                .signWith(key)
                .compact();
    }

    @Test
    void shouldAuthenticateTheUserWhenTheTokenIsValid() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token(SECRET, 3_600_000L));

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("owner@gmail.com", authentication.getPrincipal());
        assertEquals(7L, authentication.getDetails());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_OWNER")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenThereIsNoAuthorizationHeader() throws Exception {
        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTheHeaderIsNotABearerToken() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic abc123");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTheTokenIsSignedWithAnotherSecret() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token(OTHER_SECRET, 3_600_000L));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTheTokenIsExpired() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token(SECRET, -1000L));

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTheTokenIsMalformed() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
