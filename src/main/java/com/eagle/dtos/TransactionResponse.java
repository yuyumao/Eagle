package com.eagle.dtos;

import com.eagle.entity.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class TransactionResponse {
    String transactionId;
    BigDecimal amount;
    Currency currency;
    TransactionType type;
    Instant createTimeStamp;
}
