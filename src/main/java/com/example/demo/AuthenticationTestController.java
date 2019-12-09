package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class AuthenticationTestController {
    @RequestMapping(
            method = RequestMethod.GET
    )
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("You can only see this after a successful login :)");
    }
}
