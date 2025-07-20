package com.eagle.service;

import com.eagle.entity.Account;
import com.eagle.entity.Transaction;
import com.eagle.exceptions.AccountNotFoundException;
import com.eagle.exceptions.ConcurrentTransactionException;
import com.eagle.exceptions.CurrencyMismatchException;
import com.eagle.dtos.*;
import com.eagle.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class TransactionService {

    private final AccountRepository accountRepository;

    public TransactionService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Transaction createTransaction(String accountNumber, CreateTransactionRequest request) {
        log.info("Trying to find account by number:{}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        log.info("Found account by number:{}", accountNumber);

        final String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!account.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("User does not own this account");
        }
        log.info("Account user matched: {}", accountNumber);
        if (!request.getCurrency().equals(account.getCurrency())) {
            throw new CurrencyMismatchException("Transaction currency does not match account currency");
        }
        log.info("Account currency matched: {}", accountNumber);

        Transaction transaction = new Transaction(
                account,
                request.getType(),
                request.getCurrency(),
                request.getAmount(),
                request.getReference()
        );

        log.info("Transaction {} created for account:{}", transaction.getId(), accountNumber);

        try {
            if (transaction.getType() == TransactionType.deposit) {
                account.deposit(request.getAmount());
            } else if (transaction.getType() == TransactionType.withdraw) {
                account.withdraw(request.getAmount());
            } else {
                throw new RuntimeException("Invalid transaction type");
            }
            log.info("Account balance updated {}", accountNumber);
            account.addTransaction(transaction);
            accountRepository.save(account);
            log.info("Account transaction added: {}", transaction.getId());
        } catch (OptimisticLockingFailureException ex) {
            throw new ConcurrentTransactionException("Account balance updated by another transaction");
        }

        log.info("Account updated");

        return transaction;
    }
}
