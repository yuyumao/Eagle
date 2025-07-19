package com.eagle.controller;

import com.eagle.pojo.CreateUserRequest;
import com.eagle.entity.User;
import com.eagle.pojo.UserAddressDTO;
import com.eagle.pojo.UserResponse;
import com.eagle.service.UserCreationService;
import com.eagle.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/v1/user")
public class UserController {

    @Autowired
    private UserCreationService userCreationServiceService;

    @PostMapping
    public ResponseEntity<UserResponse> add(@Valid @RequestBody CreateUserRequest request) {
        User user = userCreationServiceService.create(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/v1/user")
                .buildAndExpand(user.getUserId())
                .toUri();
        UserResponse response = convertToResponse(user);
        return ResponseEntity.created(location).body(response);
    }

    private UserResponse convertToResponse(User user) {

        UserAddressDTO address = new UserAddressDTO(
                user.getAddress().getLine1(),
                user.getAddress().getLine2(),
                user.getAddress().getLine3(),
                user.getAddress().getTown(),
                user.getAddress().getCounty(),
                user.getAddress().getPostcode()
        );

        UserResponse response = new UserResponse(
                user.getUserId(),
                user.getUserName(),
                address,
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCreatedTimestamp(),
                user.getUpdatedTimestamp()
        );

        return response;
    }

}
