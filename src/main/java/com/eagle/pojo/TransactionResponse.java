package com.eagle.pojo;

import com.eagle.entity.Currency;
import java.math.BigDecimal;
import java.time.Instant;

public class TransactionResponse {
    String transactionId;
    BigDecimal amount;
    Currency currency;
    TransactionType type;
    Instant createTimeStamp;
}
