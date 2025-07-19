package com.eagle.controller;

import com.eagle.entity.Account;
import com.eagle.entity.User;
import com.eagle.pojo.AccountResponse;
import com.eagle.pojo.CreateAccountRequest;
import com.eagle.pojo.CreateUserRequest;
import com.eagle.pojo.UserResponse;
import com.eagle.service.AccountService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/v1/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.create(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/v1/account")
                .buildAndExpand(request.getName())
                .toUri();
        AccountResponse response = convertToResponse(account);
        return ResponseEntity.created(location).body(response);
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
