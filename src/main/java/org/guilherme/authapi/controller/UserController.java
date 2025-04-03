package org.guilherme.authapi.controller;


import jakarta.validation.Valid;
import org.guilherme.authapi.dto.RegisterRequest;
import org.guilherme.authapi.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.guilherme.authapi.repository.UserRepository;
import org.guilherme.authapi.dto.RegisterRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserRepository userRepository;



    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {

        List<String> passwordErrors = passwordValidator.validate(registerRequest.getPassword());
        if (!passwordErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(passwordErrors);
        }

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists");
        }


        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());

        User savedUser = userRepository.save(user);

        return ResponseEntity.ok().body(new RegisterRequest(savedUser.getUsername(), null, savedUser.getEmail()));
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationRequest authRequest) {

        String username = authRequest.getUsername();
        String password = authRequest.getPassword();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new AuthenticationResponse(token));
    }



}
