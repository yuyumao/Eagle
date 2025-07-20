package com.eagle.entity;

import com.eagle.dtos.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transaction")
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotNull
    @Positive
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "10000.00")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @NotNull
    @Column(nullable = false, updatable = false)
    private Instant timestamp = Instant.now();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    private String reference;

    @PrePersist
    protected void onCreate() {
        this.timestamp = Instant.now();
    }

    public Transaction(Account account, TransactionType type, Currency currency, BigDecimal amount, String reference) {
        this.account = account;
        this.type = type;
        this.currency = currency;
        this.amount = amount;
        this.reference = reference;
    }

    public Transaction(){}
}