package com.eagle.service;

import com.eagle.entity.Account;
import com.eagle.entity.Currency;
import com.eagle.entity.SortCode;
import com.eagle.entity.User;
import com.eagle.dtos.CreateAccountRequest;
import com.eagle.repository.AccountRepository;
import com.eagle.repository.AccountSequenceRepository;
import com.eagle.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class AccountService {

    public final static String ACCOUNT_PREFIX = "01";

    private final AccountSequenceRepository accountSequenceRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountSequenceRepository accountSequenceRepository, AccountRepository accountRepository, UserRepository userRepository) {
        this.accountSequenceRepository = accountSequenceRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public Account create(CreateAccountRequest createAccountRequest) {

        final String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User was not found ID: " + userId));
        //max 1 million accounts
        Long nextSequenceValue = accountSequenceRepository.getNextSequenceValue();
        if (nextSequenceValue >= 1000000) {
            throw new IllegalStateException();
        }
        String accNo = ACCOUNT_PREFIX + String.format("%06d", nextSequenceValue);
        Account account = new Account(
                user,
                accNo,
                SortCode.Branch1,
                createAccountRequest.getName(),
                createAccountRequest.getAccountType(),
                new BigDecimal(0),
                Currency.GBP
        );
        return accountRepository.save(account);
    }
}
