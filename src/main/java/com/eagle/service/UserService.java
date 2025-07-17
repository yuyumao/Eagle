package com.eagle.service;

import com.eagle.pojo.User;
import com.eagle.pojo.UserDTO;
import com.eagle.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public User add(UserDTO userDTO) {

        User user = new User();
        BeanUtils.copyProperties(user, userRepository);

        return userRepository.save(user);
    }
}
