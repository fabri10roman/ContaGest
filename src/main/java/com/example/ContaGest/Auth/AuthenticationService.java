package com.example.ContaGest.Auth;


import com.example.ContaGest.config.JwtService;
import com.example.ContaGest.model.AccountantModel;
import com.example.ContaGest.model.ClientModel;
import com.example.ContaGest.model.Role;
import com.example.ContaGest.repository.AccountantRepository;
import com.example.ContaGest.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AccountantRepository accountantRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    public AuthenticationResponse registerAccountant(RegisterRequestAccountant request) {
        var user = AccountantModel.builder()
                .userCI(request.getUserCI())
                .email(request.getEmail())
                .name(request.getName())
                .lastname(request.getLastname())
                .number(request.getNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROL_ACCOUNTANT)
                .build();
        accountantRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticateAccountant(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getCi(),request.getPassword()));
        var user = accountantRepository.findByUsername(request.getCi()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse registerClient(RegisterRequestClient request) {
        var user = ClientModel.builder()
                .userCI(request.getUserCI())
                .email(request.getEmail())
                .name(request.getName())
                .lastname(request.getLastname())
                .number(request.getNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROL_CLIENT)
                .build();
        clientRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticateClient(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getCi(),request.getPassword()));
        var user = clientRepository.findByUsername(request.getCi()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
