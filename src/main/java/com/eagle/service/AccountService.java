package com.eagle.service;

import com.eagle.entity.Account;
import com.eagle.entity.Currency;
import com.eagle.entity.SortCode;
import com.eagle.entity.User;
import com.eagle.pojo.CreateAccountRequest;
import com.eagle.pojo.ErrorResponse;
import com.eagle.repository.AccountRepository;
import com.eagle.repository.AccountSequenceRepository;
import com.eagle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class AccountService {

    public final static String ACCOUNT_PREFIX = "01";

    @Autowired
    private AccountSequenceRepository accountSequenceRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;

    public Account create(CreateAccountRequest createAccountRequest) {
        final String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Create Account: userId: " + userId);
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
                Currency.GBP,
                Instant.now(),
                Instant.now()
        );
        return accountRepository.save(account);
    }
}
