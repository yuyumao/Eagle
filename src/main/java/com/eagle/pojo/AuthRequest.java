package com.eagle.pojo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String password;
}
