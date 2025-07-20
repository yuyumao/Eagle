package com.eagle.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private JWTService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String VALID_JWT = "valid.jwt.token";
    private final String USERNAME = "user123";
    private final String BEARER_TOKEN = "Bearer " + VALID_JWT;
    private final UserDetails userDetails = User.builder()
            .username(USERNAME)
            .password("password")
            .authorities(Collections.emptyList())
            .build();

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_NoAuthorizationHeader_ContinuesChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidAuthorizationHeader_ContinuesChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtService.isTokenValid(VALID_JWT, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verifySecurityContextSet();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ValidTokenButExistingAuthentication_ContinuesWithoutChange() throws ServletException, IOException {
        // Set up existing authentication
        setExistingAuthentication();

        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verify no new authentication was set
        verify(jwtService, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ValidTokenButInvalidUser_ContinuesWithoutAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenThrow(new UsernameNotFoundException("User not found"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenButInvalidJwt_ContinuesWithoutAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtService.isTokenValid(VALID_JWT, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenWithDetails_SetsRequestDetails() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtService.isTokenValid(VALID_JWT, userDetails)).thenReturn(true);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verifySecurityContextSet();
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth.getDetails());

        assertInstanceOf(WebAuthenticationDetails.class, auth.getDetails());
    }

    private void verifySecurityContextSet() {
        SecurityContext context = SecurityContextHolder.getContext();
        assertNotNull(context.getAuthentication());
        assertEquals(USERNAME, context.getAuthentication().getName());
        assertEquals(
                userDetails.getAuthorities().size(),
                context.getAuthentication().getAuthorities().size(),
                "Authority sizes differ"
        );
        assertTrue(
                context.getAuthentication().getAuthorities().containsAll(userDetails.getAuthorities()),
                "Authorities content differs"
        );
    }

    private void setExistingAuthentication() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken("otherUser", null, Collections.emptyList());
        context.setAuthentication(existingAuth);
        SecurityContextHolder.setContext(context);
    }
}