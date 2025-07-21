package com.eagle.service;

import com.eagle.entity.Account;
import com.eagle.entity.Transaction;
import com.eagle.exceptions.AccountNotFoundException;
import com.eagle.exceptions.ConcurrentTransactionException;
import com.eagle.exceptions.CurrencyMismatchException;
import com.eagle.dtos.*;
import com.eagle.repository.AccountRepository;
import jakarta.transaction.Transactional;
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

    @Transactional
    public Transaction createTransaction(String accountNumber, CreateTransactionRequest request) {
        Account account = accountRepository.findWithLockingByAccountNumber(accountNumber)
                    .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        final String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!account.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("User does not own this account");
        }
        if (!request.getCurrency().equals(account.getCurrency())) {
            throw new CurrencyMismatchException("Transaction currency does not match account currency");
        }
        Transaction transaction = new Transaction(
                account,
                request.getType(),
                request.getCurrency(),
                request.getAmount(),
                request.getReference()
        );
        Transaction savedTransaction;
        try {
            if (transaction.getType() == TransactionType.deposit) {
                account.deposit(request.getAmount());
            } else if (transaction.getType() == TransactionType.withdraw) {
                account.withdraw(request.getAmount());
            } else {
                throw new RuntimeException("Invalid transaction type");
            }
            account.addTransaction(transaction);
            Account savedAccount = accountRepository.save(account);

            savedTransaction = savedAccount.getTransactions().stream()
                    .filter(tx -> tx.getReference().equals(transaction.getReference()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Transaction not persisted"));
        } catch (OptimisticLockingFailureException ex) {
            throw new ConcurrentTransactionException("Account balance updated by another transaction");
        }
        return savedTransaction;
    }
}
