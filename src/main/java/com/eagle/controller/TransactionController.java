package com.eagle.controller;

import com.eagle.entity.Transaction;
import com.eagle.dtos.CreateTransactionRequest;
import com.eagle.dtos.TransactionResponse;
import com.eagle.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/account")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/{accountNumber}/transactions")
    public ResponseEntity<TransactionResponse> createTransaction(
            @PathVariable @Pattern(regexp = "^01\\d{6}$") String accountNumber,
            @Valid @RequestBody CreateTransactionRequest request) {

        Transaction transaction = transactionService.createTransaction(accountNumber, request);

        TransactionResponse transactionResponse = new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getType(),
                transaction.getTimestamp()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionResponse);
    }
}
