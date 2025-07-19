package com.eagle.service;

import com.eagle.entity.Account;
import com.eagle.entity.Currency;
import com.eagle.entity.SortCode;
import com.eagle.pojo.AccountResponse;
import com.eagle.pojo.CreateAccountRequest;
import com.eagle.repository.AccountRepository;
import com.eagle.repository.AccountSequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AccountService {

    public final static String ACCOUNT_PREFIX = "01";

    @Autowired
    private AccountSequenceRepository accountSequenceRepository;
    @Autowired
    private AccountRepository accountRepository;

    public Account create(CreateAccountRequest createAccountRequest) {
        //max 1 million accounts
        if (accountSequenceRepository.getNextSequenceValue() >= 1000000) {
            throw new IllegalStateException();
        }
        String accNo = ACCOUNT_PREFIX + String.format("%06d", accountSequenceRepository.getNextSequenceValue());

        Account account = new Account(
                accNo,
                SortCode.Branch1,
                createAccountRequest.getName(),
                createAccountRequest.getAccountType(),
                0.00f,
                Currency.GBP,
                Instant.now(),
                Instant.now()
        );
        return accountRepository.save(account);
    }
}
