package com.eagle.service;

import com.eagle.entity.User;
import com.eagle.dtos.CreateUserRequest;
import com.eagle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserCreationService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserCreationService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public User create(CreateUserRequest createUserRequest) {
        String hashedPassword = encoder.encode(createUserRequest.getPassword());

        User.Address address = new User.Address(
                createUserRequest.getAddress().getLine1(),
                createUserRequest.getAddress().getLine2(),
                createUserRequest.getAddress().getLine3(),
                createUserRequest.getAddress().getTown(),
                createUserRequest.getAddress().getCounty(),
                createUserRequest.getAddress().getPostcode()
        );

        User user = new User(
                createUserRequest.getName(),
                address,
                createUserRequest.getPhoneNumber(),
                createUserRequest.getEmail(),
                hashedPassword
        );
        return userRepository.save(user);
    }
}
