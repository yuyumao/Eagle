package com.eagle.service;

import com.eagle.entity.Account;
import com.eagle.entity.Transaction;
import com.eagle.pojo.*;
import com.eagle.repository.AccountRepository;
import com.eagle.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
@Transactional
public class TransactionService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction createTransaction(String accountNumber, CreateTransactionRequest request) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        final String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!account.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("User does not own this account");
        }

        if (!request.getCurrency().equals(account.getCurrency().name())) {
            throw new CurrencyMismatchException("Transaction currency does not match account currency");
        }

        Transaction transaction = new Transaction(
                account,
                Instant.now(),
                request.getType(),
                request.getCurrency(),
                request.getAmount()
        );

        try {
            if (transaction.getType() == TransactionType.deposit) {
                account.deposit(request.getAmount());
            } else {
                account.withdraw(request.getAmount());
            }
        } catch (OptimisticLockingFailureException ex) {
            throw new ConcurrentTransactionException("Account balance updated by another transaction");
        }

        account.getTransactions().add(transaction);
        accountRepository.save(account);

        return transaction;
    }
}
