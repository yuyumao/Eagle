package com.eagle.pojo;

import com.eagle.entity.AccountType;
import com.eagle.entity.Currency;
import com.eagle.entity.SortCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Sort;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class AccountResponse {

    private String accountNumber;

    private SortCode sortCode;

    private String name;

    private AccountType accountType;

    private float balance;

    private Currency currency;

    private Instant createdTimestamp;

    private Instant updatedTimestamp;
}