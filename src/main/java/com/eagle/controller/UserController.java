package com.eagle.controller;

import com.eagle.pojo.CreateUserRequest;
import com.eagle.entity.User;
import com.eagle.pojo.ErrorResponse;
import com.eagle.pojo.UserAddressDTO;
import com.eagle.pojo.UserResponse;
import com.eagle.service.UserCreationService;
import com.eagle.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.net.URI;


@RestController
@RequestMapping("/v1/user")
public class UserController {

    @Autowired
    private UserCreationService userCreationServiceService;
    @Autowired
    private UserService userService;

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

    @GetMapping("/{userId}")
    public ResponseEntity<?> get(@PathVariable("userId") String userId, Authentication authentication) {
        return userService.findByUserId(userId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
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
