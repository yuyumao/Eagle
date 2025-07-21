package com.eagle.service;

import com.eagle.entity.Account;
import com.eagle.entity.Currency;
import com.eagle.entity.Transaction;
import com.eagle.entity.User;
import com.eagle.exceptions.AccountNotFoundException;
import com.eagle.exceptions.ConcurrentTransactionException;
import com.eagle.exceptions.InsufficientFundsException;
import com.eagle.dtos.*;
import com.eagle.repository.AccountRepository;
import com.eagle.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransactionService transactionService;

    private final String userId = UUID.randomUUID().toString();
    private final String accountNumber = "01123456";
    private Account testAccount;
    private CreateTransactionRequest depositRequest;
    private CreateTransactionRequest withdrawalRequest;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(userId);

        User user = new User();
        user.setUserId(userId);

        testAccount = new Account();
        testAccount.setAccountNumber(accountNumber);
        testAccount.setUser(user);
        testAccount.setCurrency(Currency.GBP);
        testAccount.setBalance(BigDecimal.valueOf(1000.00));
        testAccount.setId(UUID.randomUUID().toString());

        depositRequest = new CreateTransactionRequest();
        depositRequest.setAmount(BigDecimal.valueOf(100.00));
        depositRequest.setCurrency(Currency.GBP);
        depositRequest.setType(TransactionType.deposit);
        depositRequest.setReference("Salary");

        withdrawalRequest = new CreateTransactionRequest();
        withdrawalRequest.setAmount(BigDecimal.valueOf(200.00));
        withdrawalRequest.setCurrency(Currency.GBP);
        withdrawalRequest.setType(TransactionType.withdraw);
        withdrawalRequest.setReference("Rent");
    }

    @Test
    void createTransaction_Deposit_Success() {
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account savedAccount = invocation.getArgument(0);
            savedAccount.getTransactions().forEach(tx -> tx.setId("id")); // 设置模拟ID
            return savedAccount;
        });

        Transaction transaction = transactionService.createTransaction(accountNumber, depositRequest);

        assertNotNull(transaction);
        assertEquals(TransactionType.deposit, transaction.getType());
        assertEquals(BigDecimal.valueOf(100.00), transaction.getAmount());
        assertEquals("Salary", transaction.getReference());

        assertEquals(BigDecimal.valueOf(1100.00), testAccount.getBalance());

        verify(accountRepository).save(testAccount);
        assertTrue(testAccount.getTransactions().contains(transaction));
    }

    @Test
    void createTransaction_Withdrawal_Success() {
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account savedAccount = invocation.getArgument(0);
            savedAccount.getTransactions().forEach(tx -> tx.setId("id")); // 设置模拟ID
            return savedAccount;
        });

        Transaction transaction = transactionService.createTransaction(accountNumber, withdrawalRequest);

        assertNotNull(transaction);
        assertEquals(TransactionType.withdraw, transaction.getType());
        assertEquals(BigDecimal.valueOf(200.00), transaction.getAmount());

        assertEquals(BigDecimal.valueOf(800.00), testAccount.getBalance());
    }

    @Test
    void createTransaction_AccountNotFound() {
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.createTransaction(accountNumber, depositRequest)
        );
    }

    @Test
    void createTransaction_UnauthorizedUser() {
        User otherUser = new User();
        otherUser.setUserId(UUID.randomUUID().toString());
        testAccount.setUser(otherUser);

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> transactionService.createTransaction(accountNumber, depositRequest)
        );

        assertEquals("User does not own this account", exception.getMessage());
    }

    @Test
    void createTransaction_InsufficientFunds() {
        withdrawalRequest.setAmount(BigDecimal.valueOf(1500.00));

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));

        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> transactionService.createTransaction(accountNumber, withdrawalRequest)
        );

        assertTrue(exception.getMessage().contains("Insufficient balance"));
    }

    @Test
    void createTransaction_ConcurrentUpdate() {
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));

        doThrow(new OptimisticLockingFailureException("Concurrent update"))
                .when(accountRepository).save(testAccount);

        ConcurrentTransactionException exception = assertThrows(
                ConcurrentTransactionException.class,
                () -> transactionService.createTransaction(accountNumber, depositRequest)
        );

        assertEquals("Account balance updated by another transaction", exception.getMessage());
    }

    @Test
    void createTransaction_InvalidTransactionType() {
        CreateTransactionRequest invalidRequest = new CreateTransactionRequest();
        invalidRequest.setAmount(BigDecimal.valueOf(100.00));
        invalidRequest.setCurrency(Currency.GBP);
        invalidRequest.setType(null);

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(testAccount));

        assertThrows(
                RuntimeException.class,
                () -> transactionService.createTransaction(accountNumber, invalidRequest)
        );
    }
}