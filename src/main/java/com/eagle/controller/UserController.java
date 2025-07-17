package com.eagle.controller;

import com.eagle.pojo.User;
import com.eagle.pojo.UserDTO;
import com.eagle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping
    public String add(@RequestBody UserDTO user) {
        userService.add(user);
        return "";
    }

}
