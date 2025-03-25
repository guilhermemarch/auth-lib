package org.guilherme.authapi.controller;


import jakarta.validation.Valid;
import org.guilherme.authapi.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserController {


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest) {






}
