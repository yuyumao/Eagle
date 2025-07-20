package com.eagle.pojo;

import com.eagle.entity.Currency;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransactionRequest {

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.00", inclusive = true, message = "Amount must be at least 0.00")
    @DecimalMax(value = "10000.00", inclusive = true, message = "Amount cannot exceed 10000.00")
    BigDecimal amount;
    @NotNull(message = "Currency type cannot be null")
    Currency currency;
    @NotNull(message = "Transaction type cannot be null")
    TransactionType type;
}
