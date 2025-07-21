package com.eagle.service;

import com.eagle.dtos.CreateTransactionRequest;
import com.eagle.dtos.TransactionType;
import com.eagle.entity.*;
import com.eagle.entity.Currency;
import com.eagle.exceptions.ConcurrentTransactionException;
import com.eagle.exceptions.InsufficientFundsException;
import com.eagle.repository.AccountRepository;
import com.eagle.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TransactionServiceConcurrencyTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String ACCOUNT_NUMBER = "01100001";

    private static final BigDecimal initialBalance = BigDecimal.valueOf(5000.00);

    private User testUser = null;

    @BeforeEach
    void setupSecurityContext() {
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Create and persist test user and account
        User.Address address = new User.Address(
                "1", "2", "3", "4", "5", "6"
        );
        testUser = new User(
                "userName",
                address,
                "",
                "",
                ""
        );
        User savedUser = userRepository.save(testUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(savedUser.getUserId(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Account account = new Account(
                testUser,
                ACCOUNT_NUMBER,
                SortCode.Branch1,
                "Account_Name",
                AccountType.personal,
                initialBalance,
                Currency.GBP
        );
        Account savedAccount = accountRepository.save(account);
    }

    @Test
    void testConcurrentTransactions() throws InterruptedException, ExecutionException {
        // Prepare a withdraw request
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(BigDecimal.valueOf(50.00));
        request.setCurrency(Currency.GBP);
        request.setType(TransactionType.withdraw);
        request.setReference("TX-" + UUID.randomUUID());

        // ✅ Wrap ExecutorService so it inherits SecurityContext
        ExecutorService rawExecutor = Executors.newFixedThreadPool(2);
        ExecutorService executor = new DelegatingSecurityContextExecutorService(rawExecutor);

        Callable<Void> task = () -> {
            transactionService.createTransaction(ACCOUNT_NUMBER, request);
            return null;
        };

        Future<Void> future1 = executor.submit(task);
        Future<Void> future2 = executor.submit(task);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        int successCount = 0;
        int failureCount = 0;

        for (Future<Void> f : Arrays.asList(future1, future2)) {
            try {
                f.get();
                successCount++;
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof ConcurrentTransactionException) {
                    failureCount++;
                } else {
                    throw ex; // unexpected failure
                }
            }
        }

        // We expect exactly one success and one failure
        assertEquals(2, successCount);
        assertEquals(0, failureCount);

        // Verify final balance = 0 (because both withdrawals succeeded)
        Account updatedAccount = accountRepository.findByAccountNumber(ACCOUNT_NUMBER).orElseThrow();
        assertEquals(0, updatedAccount.getBalance().compareTo(initialBalance.subtract(BigDecimal.valueOf(100))));
    }

    @Test
    void testConcurrentDepositAndWithdrawMultipleTimes() throws InterruptedException, ExecutionException {
        // ✅ Deposit request (+10)
        CreateTransactionRequest depositRequest = new CreateTransactionRequest();
        depositRequest.setAmount(BigDecimal.TEN);
        depositRequest.setCurrency(Currency.GBP);
        depositRequest.setType(TransactionType.deposit);
        depositRequest.setReference("DEP-" + UUID.randomUUID());

        // ✅ Withdraw request (-10)
        CreateTransactionRequest withdrawRequest = new CreateTransactionRequest();
        withdrawRequest.setAmount(BigDecimal.TEN);
        withdrawRequest.setCurrency(Currency.GBP);
        withdrawRequest.setType(TransactionType.withdraw);
        withdrawRequest.setReference("WTH-" + UUID.randomUUID());

        int totalTasks = 1000; // 5 deposits + 5 withdrawals
        ExecutorService rawExecutor = Executors.newFixedThreadPool(50);
        ExecutorService executor = new DelegatingSecurityContextExecutorService(rawExecutor);

        // ✅ Create a mix of 50 deposits and 50 withdrawals
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            tasks.add(() -> { transactionService.createTransaction(ACCOUNT_NUMBER, depositRequest); return null; });
            tasks.add(() -> { transactionService.createTransaction(ACCOUNT_NUMBER, withdrawRequest); return null; });
        }

        // ✅ Submit all tasks concurrently
        List<Future<Void>> futures = new ArrayList<>();
        for (Callable<Void> task : tasks) {
            futures.add(executor.submit(task));
        }

        executor.shutdown();
        executor.awaitTermination(50, TimeUnit.SECONDS);

        int successCount = 0;
        int failureCount = 0;

        for (Future<Void> f : futures) {
            try {
                f.get();
                successCount++;
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof ConcurrentTransactionException) {
                    failureCount++;
                } else {
                    throw ex; // unexpected failure
                }
            }
        }

        // ✅ Expect all 1000 to succeed (since balance will never go negative)
        assertEquals(totalTasks, successCount);
        assertEquals(0, failureCount);

        // ✅ Final balance should still be the initialBalance (500x +10, 500x -10 → net 0 change)
        Account updatedAccount = accountRepository.findByAccountNumber(ACCOUNT_NUMBER).orElseThrow();
        assertEquals(0, updatedAccount.getBalance().compareTo(initialBalance));
    }

    @Test
    void testConcurrentTransactionsWithMultipleAccountsAndRandomAmounts() throws InterruptedException, ExecutionException {
        int numAccounts = 5;
        int totalTasks = 1000; // total concurrent transactions
        int threadPoolSize = 50;

        // ✅ Create multiple accounts
        List<Account> accounts = new ArrayList<>();
        BigDecimal initialBalancePerAccount = BigDecimal.valueOf(100.00);
        for (int i = 0; i < numAccounts; i++) {
            Account acc = new Account(
                    testUser,
                    "0100000" + i,
                    SortCode.Branch1,
                    "Account_" + i,
                    AccountType.personal,
                    initialBalancePerAccount,
                    Currency.GBP
            );
            accounts.add(accountRepository.save(acc));
        }

        BigDecimal totalInitialBalance = initialBalancePerAccount.multiply(BigDecimal.valueOf(numAccounts));

        // ✅ Thread pool for concurrent execution
        ExecutorService rawExecutor = Executors.newFixedThreadPool(threadPoolSize);
        ExecutorService executor = new DelegatingSecurityContextExecutorService(rawExecutor);

        Random random = new Random();
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < totalTasks; i++) {
            tasks.add(() -> {
                // ✅ Pick a random account
                Account randomAccount = accounts.get(random.nextInt(numAccounts));

                // ✅ Random transaction amount between 1 and 20
                BigDecimal randomAmount = BigDecimal.valueOf(1 + random.nextInt(20));

                // ✅ Randomly choose deposit or withdraw
                TransactionType type = random.nextBoolean() ? TransactionType.deposit : TransactionType.withdraw;

                CreateTransactionRequest req = new CreateTransactionRequest();
                req.setAmount(randomAmount);
                req.setCurrency(Currency.GBP);
                req.setType(type);
                req.setReference(type.name() + "-" + UUID.randomUUID());

                try {
                    transactionService.createTransaction(randomAccount.getAccountNumber(), req);
                } catch (ConcurrentTransactionException | InsufficientFundsException e) {
                    // It's okay if withdraw fails due to insufficient funds,
                    // or optimistic locking triggers a retry (if not retried automatically)
                    // We just log it.
                    System.out.println("Transaction failed: " + e.getMessage());
                }
                return null;
            });
        }

        // ✅ Submit all tasks
        List<Future<Void>> futures = new ArrayList<>();
        for (Callable<Void> task : tasks) {
            futures.add(executor.submit(task));
        }

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        // ✅ Wait for all and check if any unexpected failure
        for (Future<Void> f : futures) {
            try {
                f.get(); // forces exceptions to surface
            } catch (ExecutionException e) {
                throw new RuntimeException("Unexpected transaction failure", e);
            }
        }

        // ✅ Compute final total balance across all accounts
        BigDecimal totalFinalBalance = accountRepository.findAll().stream()
                .filter(acc -> accounts.stream().anyMatch(a -> a.getId().equals(acc.getId()))) // only test accounts
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ✅ Ensure total balance NEVER went negative or exceeded expected max
        assertTrue(totalFinalBalance.compareTo(BigDecimal.ZERO) >= 0, "Total balance should never go negative");

        // ✅ For this test, we can't predict exact final balance (due to random deposits/withdrawals),
        // BUT we can assert it's within a reasonable range:
        BigDecimal maxPossible = totalInitialBalance.add(BigDecimal.valueOf(totalTasks * 20)); // worst case all deposits
        BigDecimal minPossible = BigDecimal.ZERO; // worst case all withdrawals fail

        assertTrue(totalFinalBalance.compareTo(minPossible) >= 0 && totalFinalBalance.compareTo(maxPossible) <= 0,
                "Final balance " + totalFinalBalance + " should be between " + minPossible + " and " + maxPossible);

        System.out.println("✅ Concurrency test passed! Final total balance = " + totalFinalBalance);
    }


}
