package com.example.ContaGest.controller;

import com.example.ContaGest.dto.AuthenticationResponse;
import com.example.ContaGest.dto.RegisterRequestAccountant;
import com.example.ContaGest.dto.RegisterRequestClient;
import com.example.ContaGest.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.ContaGest.dto.LoginRequest;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody LoginRequest request){
        return authenticationService.login(request);
    }

    @PostMapping("/register-accountant")
    public ResponseEntity<AuthenticationResponse> registerAccountant(@RequestBody RegisterRequestAccountant request) {
        return ResponseEntity.ok(authenticationService.registerAccountant(request));
    }

    @GetMapping("/confirm-accountant")
    public ResponseEntity<?> confirmAccountant(@RequestParam("token") String token){
        return authenticationService.confirmTokenAccountant(token);
    }

    @GetMapping("/confirm-client")
    public ResponseEntity<?> confirmClient(@RequestParam("token") String token){
        return authenticationService.confirmTokenClient(token);
    }

    @PostMapping("/register-client")
    public ResponseEntity<String> registerClient(@RequestBody RegisterRequestClient request) {
        return ResponseEntity.ok(authenticationService.registerClient(request));
    }

}
