package com.eagle.controller;

import com.eagle.entity.Account;
import com.eagle.entity.SortCode;
import com.eagle.dtos.AccountResponse;
import com.eagle.dtos.CreateAccountRequest;
import com.eagle.service.AccountService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = convertToResponse(accountService.create(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private AccountResponse convertToResponse(Account account) {
        return new AccountResponse(
                account.getAccountNumber(),
                account.getSortCode(),
                account.getName(),
                account.getAccountType(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreateTimeStamp(),
                account.getUpdateTimeStamp()
        );

    }
}
