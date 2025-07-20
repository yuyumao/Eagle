package com.eagle.dtos;

import com.eagle.entity.AccountType;
import com.eagle.entity.Currency;
import com.eagle.entity.SortCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class AccountResponse {

    private String accountNumber;

    private String sortCode;

    private String name;

    private AccountType accountType;

    private BigDecimal balance;

    private Currency currency;

    private Instant createdTimestamp;

    private Instant updatedTimestamp;
}