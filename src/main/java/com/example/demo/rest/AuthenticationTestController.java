package com.example.demo.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class AuthenticationTestController {
    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("AuthenticationTestController GET /test :)");
    }
}
