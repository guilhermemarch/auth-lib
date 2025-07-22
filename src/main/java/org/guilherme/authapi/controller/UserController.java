package org.guilherme.authapi.controller;

import jakarta.validation.Valid;
import org.guilherme.authapi.dto.AuthenticationResponse;
import org.guilherme.authapi.dto.ResetPassword;
import org.guilherme.authapi.dto.LoginRequest;
import org.guilherme.authapi.dto.RegisterRequest;
import org.guilherme.authapi.entity.User;
import org.guilherme.authapi.entity.VerificationToken;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
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
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {

        List<String> passwordErrors = passwordValidator.validate(registerRequest.getPassword());

        if (!passwordErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(passwordErrors);
        }

        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new UserAlreadyExistException("Nome de usuário já existe");
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistException("Email já cadastrado");
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
        response.put("message", "Cadastro realizado com sucesso! Verifique seu email para ativar sua conta.");
        response.put("username", savedUser.getUsername());
        response.put("email", savedUser.getEmail());
        
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest authRequest) {
        String email = authRequest.getEmail();
        String password = authRequest.getPassword();

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("Email ou senha inválidos"));

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), password)
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            final String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(new AuthenticationResponse(token));
        } catch (DisabledException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Conta não verificada. Por favor, verifique seu email."));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("message", "Email ou senha inválidos"));
        }
    }
    
    @GetMapping("/verify")
    @Transactional
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        try {
            User verifiedUser = verificationService.verifyEmail(token);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Email verificado com sucesso! Agora você pode fazer login.");
            response.put("username", verifiedUser.getUsername());
            
            return ResponseEntity.ok(response);
        } catch (TokenNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Link de verificação inválido."));
        } catch (TokenExpiredException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "O link de verificação expirou. Solicite um novo."));
        }
    }
    
    @PostMapping("/resend-verification")
    @Transactional
    public ResponseEntity<?> resendVerification(@RequestParam("email") String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.ok(Map.of("message", "Se o email existir no nosso sistema, um link de verificação foi enviado."));
        }
        
        if (user.isEmailVerified()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email já verificado."));
        }
        
        verificationService.resendVerificationToken(user);
        
        return ResponseEntity.ok(Map.of("message", "Um novo link de verificação foi enviado para seu email."));
    }


    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.ok(Map.of("message", "Se o email existir no nosso sistema, um link para redefinir a senha foi enviado."));
        }

        verificationService.createPasswordResetToken(user);

        return ResponseEntity.ok(Map.of("message", "Um link para redefinir a senha foi enviado para seu email."));
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<?> resetPassword(@RequestBody ResetPassword ResetPassword) {

        String token = ResetPassword.getToken();
        String newPassword = ResetPassword.getNewPassword();

        List<String> passwordErrors = passwordValidator.validate(newPassword);

        if (!passwordErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(passwordErrors);
        }

        User user = verificationService.resetPassword(token, newPassword);


        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired password reset token"));
        }

        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso! Agora você pode fazer login com a nova senha."));
    }
}