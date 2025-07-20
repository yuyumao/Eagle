package com.eagle.controller;

import com.eagle.pojo.AuthRequest;
import com.eagle.pojo.AuthResponse;
import com.eagle.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<AuthResponse> auth(@Valid @RequestBody AuthRequest request) {
        System.out.println(request);
        AuthResponse response = authService.authenticate(request.getUserId(), request.getPassword());
        return ResponseEntity.ok(response);
    }
}
