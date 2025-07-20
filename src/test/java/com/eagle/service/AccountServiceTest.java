package com.eagle.service;

import com.eagle.entity.Account;
import com.eagle.entity.AccountType;
import com.eagle.entity.Currency;
import com.eagle.entity.SortCode;
import com.eagle.pojo.CreateAccountRequest;
import com.eagle.repository.AccountRepository;
import com.eagle.repository.AccountSequenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountSequenceRepository accountSequenceRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private CreateAccountRequest validRequest;

    private Account expectedAccount;

    @BeforeEach
    void setUp() {
        validRequest = new CreateAccountRequest(
                "accountName",
                AccountType.personal
        );

        expectedAccount = new Account(
                "John",
                "01123456",
                SortCode.Branch1,
                "accountName",
                AccountType.personal,
                new BigDecimal(0),
                Currency.GBP,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    void createAccount_Success() {
        when(accountSequenceRepository.getNextSequenceValue()).thenReturn(123456L);


        when(accountRepository.save(any(Account.class))).thenReturn(expectedAccount);

        Account result = accountService.create(validRequest);

        assertNotNull(result);
        assertEquals("01123456", result.getAccountNumber());
        assertEquals("John Doe", result.getName());
        assertEquals(AccountType.personal, result.getAccountType());
        assertEquals(SortCode.Branch1, result.getSortCode());
        assertEquals(0.00f, result.getBalance());
        assertEquals(Currency.GBP, result.getCurrency());

        verify(accountSequenceRepository, times(1)).getNextSequenceValue();
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void createAccount_WhenSequenceExceedsLimit_ThrowsException() {
        when(accountSequenceRepository.getNextSequenceValue()).thenReturn(1000000L);

        assertThrows(IllegalStateException.class, () -> {
            accountService.create(validRequest);
        });

        verify(accountSequenceRepository, times(1)).getNextSequenceValue();
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_WhenSequenceEqualsLimit_ThrowsException() {
        when(accountSequenceRepository.getNextSequenceValue()).thenReturn(999999L);
        when(accountRepository.save(any(Account.class))).thenReturn(expectedAccount);

        Account result = accountService.create(validRequest);

        assertNotNull(result);

        when(accountSequenceRepository.getNextSequenceValue()).thenReturn(1000000L);
        assertThrows(IllegalStateException.class, () -> {
            accountService.create(validRequest);
        });

        verify(accountSequenceRepository, times(2)).getNextSequenceValue();
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void createAccount_AccountNumberFormatting() {
        when(accountSequenceRepository.getNextSequenceValue()).thenReturn(1L, 999L, 9999L, 99999L, 999999L);

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account account1 = accountService.create(validRequest);
        assertEquals("01000001", account1.getAccountNumber());

        Account account999 = accountService.create(validRequest);
        assertEquals("01000999", account999.getAccountNumber());

        Account account9999 = accountService.create(validRequest);
        assertEquals("01009999", account9999.getAccountNumber());

        Account account99999 = accountService.create(validRequest);
        assertEquals("01099999", account99999.getAccountNumber());

        Account account999999 = accountService.create(validRequest);
        assertEquals("01999999", account999999.getAccountNumber());
    }

    @Test
    void createAccount_DefaultValuesSetCorrectly() {
        // 模拟序列号
        when(accountSequenceRepository.getNextSequenceValue()).thenReturn(123456L);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行方法
        Account result = accountService.create(validRequest);

        // 验证默认值
        assertEquals(SortCode.Branch1, result.getSortCode());
        assertEquals(0.00f, result.getBalance());
        assertEquals(Currency.GBP, result.getCurrency());
        assertNotNull(result.getCreateTimeStamp());
        assertNotNull(result.getUpdateTimeStamp());
        assertTrue(result.getCreateTimeStamp().isBefore(Instant.now().plusSeconds(1)) ||
                result.getCreateTimeStamp().equals(Instant.now()));
        assertTrue(result.getUpdateTimeStamp().isBefore(Instant.now().plusSeconds(1)) ||
                result.getUpdateTimeStamp().equals(Instant.now()));
    }

}
