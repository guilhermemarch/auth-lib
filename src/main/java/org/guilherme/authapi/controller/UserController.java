package org.guilherme.authapi.controller;

import jakarta.validation.Valid;
import org.guilherme.authapi.dto.AuthenticationResponse;
import org.guilherme.authapi.dto.LoginRequest;
import org.guilherme.authapi.dto.RegisterRequest;
import org.guilherme.authapi.entity.User;
import org.guilherme.authapi.exception.TokenExpiredException;
import org.guilherme.authapi.exception.TokenNotFoundException;
import org.guilherme.authapi.exception.UserAlreadyExistException;
import org.guilherme.authapi.repository.UserRepository;
import org.guilherme.authapi.service.CustomUserDetailsService;
import org.guilherme.authapi.service.VerificationService;
import org.guilherme.authapi.util.JwtUtil;
import org.guilherme.authapi.util.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordValidator passwordValidator;
    
    @Autowired
    private VerificationService verificationService;


    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        List<String> passwordErrors = passwordValidator.validate(registerRequest.getPassword());
        if (!passwordErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(passwordErrors);
        }

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new UserAlreadyExistException("Username already exists");
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistException("Email already exists");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setEnabled(false);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);
        
        verificationService.createVerificationToken(savedUser);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registration successful, please check your email to verify your account");
        response.put("username", savedUser.getUsername());
        response.put("email", savedUser.getEmail());
        
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest authRequest) {
        String username = authRequest.getUsername();
        String password = authRequest.getPassword();

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (DisabledException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Account is not verified. Please verify your email"));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(token));
    }
    
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        try {
            User verifiedUser = verificationService.verifyEmail(token);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Email verification successful. You can now log in");
            response.put("username", verifiedUser.getUsername());
            
            return ResponseEntity.ok(response);
        } catch (TokenNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid verification link"));
        } catch (TokenExpiredException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Verification link has expired. Please request a new one"));
        }
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam("email") String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.ok(Map.of("message", "If your email exists in our system, a verification link has been sent"));
        }
        
        if (user.isEmailVerified()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is already verified"));
        }
        
        verificationService.resendVerificationToken(user);
        
        return ResponseEntity.ok(Map.of("message", "A new verification link has been sent to your email"));
    }
}
