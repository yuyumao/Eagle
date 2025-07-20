package com.eagle.security;

import com.eagle.dtos.AuthResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private AuthService authService;

    private final String TEST_USER_ID = "user123";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_TOKEN = "generated.jwt.token";

    @Test
    void authenticate_Success() {
        // Setup
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn(TEST_USER_ID);
        when(authenticationManager.authenticate(any()))
                .thenReturn(mockAuth);
        when(jwtService.generateToken(TEST_USER_ID)).thenReturn(TEST_TOKEN);

        // Execute
        AuthResponse response = authService.authenticate(TEST_USER_ID, TEST_PASSWORD);

        // Verify
        assertNotNull(response);
        assertEquals(TEST_TOKEN, response.getToken());

        // Verify authentication manager call
        verify(authenticationManager).authenticate(
                argThat(token ->
                        token instanceof UsernamePasswordAuthenticationToken &&
                                TEST_USER_ID.equals(token.getPrincipal()) &&
                                TEST_PASSWORD.equals(token.getCredentials())
                )
        );

        // Verify token generation
        verify(jwtService).generateToken(TEST_USER_ID);
    }

    @Test
    void authenticate_InvalidCredentials() {
        // Setup
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Execute & Verify
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.authenticate(TEST_USER_ID, TEST_PASSWORD)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticate_EmptyCredentials() {
        // Execute & Verify
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.authenticate(TEST_USER_ID, "")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.authenticate("", TEST_PASSWORD)
        );

        verifyNoInteractions(authenticationManager, jwtService);
    }

    @Test
    void authenticate_NullCredentials() {
        // Execute & Verify
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.authenticate(TEST_USER_ID, null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.authenticate(null, TEST_PASSWORD)
        );

        verifyNoInteractions(authenticationManager, jwtService);
    }

    @Test
    void authenticate_TokenGeneration() {
        // Setup
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn(TEST_USER_ID);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);

        String customToken = "custom.jwt.token";
        when(jwtService.generateToken(TEST_USER_ID)).thenReturn(customToken);

        // Execute
        AuthResponse response = authService.authenticate(TEST_USER_ID, TEST_PASSWORD);

        // Verify
        assertEquals(customToken, response.getToken());
        verify(jwtService).generateToken(TEST_USER_ID);
    }

    @Test
    void authenticate_AuthenticationDetails() {
        // Setup
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn(TEST_USER_ID);

        // Capture the authentication token passed to authenticationManager
        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        when(authenticationManager.authenticate(captor.capture())).thenReturn(mockAuth);
        when(jwtService.generateToken(anyString())).thenReturn(TEST_TOKEN);

        // Execute
        authService.authenticate(TEST_USER_ID, TEST_PASSWORD);

        // Verify authentication details
        UsernamePasswordAuthenticationToken authToken = captor.getValue();
        assertEquals(TEST_USER_ID, authToken.getPrincipal());
        assertEquals(TEST_PASSWORD, authToken.getCredentials());
        assertTrue(authToken.getAuthorities().isEmpty());
    }
}