package com.eagle.service;

import com.eagle.entity.Account;
import com.eagle.entity.AccountType;
import com.eagle.entity.Currency;
import com.eagle.entity.SortCode;
import com.eagle.entity.User;
import com.eagle.dtos.CreateAccountRequest;
import com.eagle.repository.AccountRepository;
import com.eagle.repository.AccountSequenceRepository;
import com.eagle.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountSequenceRepository accountSequenceRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private final String TEST_USER_ID = "user123";
    private final String TEST_ACCOUNT_NAME = "Test Account";
    private final AccountType TEST_ACCOUNT_TYPE = AccountType.personal;

    @BeforeEach
    void setupAuthentication() {
        Authentication auth = new UsernamePasswordAuthenticationToken(TEST_USER_ID, "password");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createAccount_Success() {
        // Setup
        CreateAccountRequest request = new CreateAccountRequest(TEST_ACCOUNT_NAME, TEST_ACCOUNT_TYPE);
        User mockUser = new User();
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(accountSequenceRepository.getNextSequenceValue()).thenReturn(123456L);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        Account result = accountService.create(request);

        // Verify
        assertNotNull(result);
        assertEquals(TEST_ACCOUNT_NAME, result.getName());
        assertEquals(TEST_ACCOUNT_TYPE, result.getAccountType());
        assertEquals(Currency.GBP, result.getCurrency());
        assertEquals(SortCode.Branch1.toString(), result.getSortCode());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals("01123456", result.getAccountNumber());
        assertEquals(mockUser, result.getUser());

        verify(userRepository).findByUserId(TEST_USER_ID);
        verify(accountSequenceRepository).getNextSequenceValue();
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_UserNotFound() {
        // Setup
        CreateAccountRequest request = new CreateAccountRequest(TEST_ACCOUNT_NAME, TEST_ACCOUNT_TYPE);
        when(userRepository.findByUserId(anyString())).thenReturn(Optional.empty());

        // Execute & Verify
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.create(request));

        assertEquals("User was not found ID: " + TEST_USER_ID, exception.getMessage());
        verify(userRepository).findByUserId(TEST_USER_ID);
        verifyNoInteractions(accountSequenceRepository, accountRepository);
    }

    @Test
    void createAccount_SequenceExhausted() {
        // Setup
        CreateAccountRequest request = new CreateAccountRequest(TEST_ACCOUNT_NAME, TEST_ACCOUNT_TYPE);
        User mockUser = new User();
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(mockUser));
        when(accountSequenceRepository.getNextSequenceValue()).thenReturn(1000000L);

        // Execute & Verify
        assertThrows(IllegalStateException.class,
                () -> accountService.create(request));

        verify(userRepository).findByUserId(TEST_USER_ID);
        verify(accountSequenceRepository).getNextSequenceValue();
        verifyNoInteractions(accountRepository);
    }

    @Test
    void createAccount_SequenceFormatting() {
        // Setup
        CreateAccountRequest request = new CreateAccountRequest(TEST_ACCOUNT_NAME, TEST_ACCOUNT_TYPE);
        User mockUser = new User();

        // Test different sequence values
        testSequenceFormatting(mockUser, 1L, "01000001");
        testSequenceFormatting(mockUser, 999999L, "01999999");
        testSequenceFormatting(mockUser, 42L, "01000042");
        testSequenceFormatting(mockUser, 9999L, "01009999");
    }

    private void testSequenceFormatting(User user, Long sequence, String expectedAccountNumber) {
        when(userRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(accountSequenceRepository.getNextSequenceValue()).thenReturn(sequence);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountService.create(new CreateAccountRequest(TEST_ACCOUNT_NAME, TEST_ACCOUNT_TYPE));

        assertEquals(expectedAccountNumber, result.getAccountNumber());
    }
}