package com.eagle.service;

import com.eagle.entity.User;
import com.eagle.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final String TEST_USER_ID = "user123";
    private final String TEST_PASSWORD_HASH = "hashed_password";
    private final String OTHER_USER_ID = "user456";

    @BeforeEach
    void setupAuthentication() {
        setAuthentication(TEST_USER_ID);
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(String username) {
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn(username);

        SecurityContext context = mock(SecurityContext.class);
        lenient().when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    @Test
    void loadUserByUsername_UserFound() {
        // Setup
        User mockUser = new User();
        mockUser.setUserId(TEST_USER_ID);
        mockUser.setPasswordHash(TEST_PASSWORD_HASH);

        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(mockUser));

        // Execute
        UserDetails userDetails = userService.loadUserByUsername(TEST_USER_ID);

        // Verify
        assertNotNull(userDetails);
        assertEquals(TEST_USER_ID, userDetails.getUsername());
        assertEquals(TEST_PASSWORD_HASH, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Setup
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());

        // Execute & Verify
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(TEST_USER_ID)
        );

        assertEquals("User not found: " + TEST_USER_ID, exception.getMessage());
    }

    @Test
    void findByUserId_SameUser_Success() {
        // Setup
        User mockUser = new User();
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(mockUser));

        // Execute
        Optional<User> result = userService.findByUserId(TEST_USER_ID);

        // Verify
        assertTrue(result.isPresent());
        assertEquals(mockUser, result.get());
    }

    @Test
    void findByUserId_SameUser_NotFound() {
        // Setup
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.empty());

        // Execute
        Optional<User> result = userService.findByUserId(TEST_USER_ID);

        // Verify
        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserId_DifferentUser_AccessDenied() {
        // Execute & Verify
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> userService.findByUserId(OTHER_USER_ID)
        );

        assertEquals("User not allowed to access this resource", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void findByUserId_NoAuthentication_AccessDenied() {
        // Setup
        SecurityContextHolder.clearContext();

        // Execute & Verify
        assertThrows(NullPointerException.class,
                () -> userService.findByUserId(TEST_USER_ID));
    }

    @Test
    void toUserDetails_ValidUser() {
        // Setup
        User user = new User();
        user.setUserId(TEST_USER_ID);
        user.setPasswordHash(TEST_PASSWORD_HASH);

        // Execute
        UserDetails userDetails = userService.toUserDetails(user);

        // Verify
        assertEquals(TEST_USER_ID, userDetails.getUsername());
        assertEquals(TEST_PASSWORD_HASH, userDetails.getPassword());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    @Test
    void findByUserId_SameUser_RepositoryCalledOnce() {
        // Setup
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(new User()));

        // Execute
        userService.findByUserId(TEST_USER_ID);

        // Verify
        verify(userRepository, times(1)).findByUserId(TEST_USER_ID);
        verifyNoMoreInteractions(userRepository);
    }
}