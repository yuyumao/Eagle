package com.eagle.entity;

import com.eagle.exceptions.InsufficientFundsException;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Table(name = "account")
@Entity
@Getter
@Setter
@ToString(exclude = "transactions")
public class Account {

    @Id
    @NotBlank
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "account_id", nullable = false, unique = true)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Pattern(regexp = "^01\\d{6}$")
    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @NotNull
    @Column(name = "sort_code", nullable = false)
    private String sortCode;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @NotNull
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "10000.00")
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "current", nullable = false)
    private Currency currency;

    @NotNull
    @Column(name = "create_timestamp", nullable = false)
    private Instant createTimeStamp;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    public Account(User user, String accountNumber, SortCode sortCode, String name, AccountType accountType, BigDecimal balance, Currency currency) {
        this.user = user;
        this.accountNumber = accountNumber;
        this.sortCode = sortCode.value;
        this.name = name;
        this.accountType = accountType;
        this.balance = balance;
        this.currency = currency;
    }

    @NotNull
    @Column(name = "update_timestamp", nullable = false)
    private Instant updateTimeStamp;

    @PreUpdate
    protected void onUpdate() {
        this.updateTimeStamp = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createTimeStamp = Instant.now();
        this.updateTimeStamp = Instant.now();
    }

    public Account() {

    }

    @Version
    private Long version;

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setAccount(this);
    }
}
