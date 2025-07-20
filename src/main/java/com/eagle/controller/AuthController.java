package com.eagle.controller;

import com.eagle.dtos.AuthRequest;
import com.eagle.dtos.AuthResponse;
import com.eagle.dtos.ErrorResponse;
import com.eagle.security.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<AuthResponse> auth(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.authenticate(request.getUserId(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Invalid Credentials"
                ));
    }
}
