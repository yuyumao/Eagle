package com.eagle.dtos;

import com.eagle.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CreateAccountRequest {

    @NotBlank
    private String name;

    @NotNull
    private AccountType accountType;
}
