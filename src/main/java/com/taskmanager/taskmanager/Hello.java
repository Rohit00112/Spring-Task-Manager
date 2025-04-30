package com.taskmanager.taskmanager;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class Hello {
    @GetMapping("/")
    public String hello(){
        return "Hello World";
    }
}
