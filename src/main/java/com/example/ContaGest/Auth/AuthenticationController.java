package com.example.ContaGest.Auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    @PostMapping("/register-accountant")
    public ResponseEntity<AuthenticationResponse> registerAccountant(@RequestBody RegisterRequestAccountant request) {
        return ResponseEntity.ok(authenticationService.registerAccountant(request));
    }
    @PostMapping("/authenticate-accountant")
    public ResponseEntity<AuthenticationResponse> authenticateAccountant(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticateAccountant(request));
    }
    @PostMapping("/register-client")
    public ResponseEntity<AuthenticationResponse> registerClient(@RequestBody RegisterRequestClient request) {
        return ResponseEntity.ok(authenticationService.registerClient(request));
    }
    @PostMapping("/authenticate-client")
    public ResponseEntity<AuthenticationResponse> authenticateClient(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticateClient(request));
    }
}
