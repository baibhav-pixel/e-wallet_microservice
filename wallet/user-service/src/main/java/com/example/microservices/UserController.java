package com.example.microservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/user")
    public void createUser(@RequestBody @Valid UserCreateRequest userCreateRequest) throws JsonProcessingException {
        userService.createUser(userCreateRequest);
    }

    @GetMapping("/user")
    public User getUser(@RequestParam int id)
    {
        return userService.getUser(id);
    }
}
