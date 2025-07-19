package com.eagle.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Instant;

@Table(name = "account")
@Entity
@Data
public class Account {

    @Id
    @NotBlank
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "account_id", nullable = false, unique = true)
    private String id;

    @NotNull
    @Pattern(regexp = "^01\\d{6}$")
    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @NotNull
    @Column(name = "sort_code", nullable = false)
    private SortCode sortCode;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @NotNull
    @DecimalMax(value = "10000.00", inclusive = true)
    @DecimalMin(value = "0.00", inclusive = true)
    @Column(name = "balance", nullable = false)
    private float balance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "current", nullable = false)
    private Currency currency;

    @NotNull
    @Column(name = "create_timestamp", nullable = false)
    private Instant createTimeStamp;

    public Account(String accountNumber, SortCode sortCode, String name, AccountType accountType, float balance, Currency currency, Instant createTimeStamp, Instant updateTimeStamp) {
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
        this.name = name;
        this.accountType = accountType;
        this.balance = balance;
        this.currency = currency;
        this.createTimeStamp = createTimeStamp;
        this.updateTimeStamp = updateTimeStamp;
    }

    @NotNull
    @Column(name = "update_timestamp", nullable = false)
    private Instant updateTimeStamp;

    public Account() {

    }
}
