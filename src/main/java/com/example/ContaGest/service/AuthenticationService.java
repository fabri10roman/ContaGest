package com.example.ContaGest.service;


import com.example.ContaGest.dto.*;
import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.model.*;
import com.example.ContaGest.repository.AccountantRepository;
import com.example.ContaGest.repository.ClientRepository;
import com.example.ContaGest.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.ContaGest.exception.UserNotFoundException;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AccountantRepository accountantRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    public AuthenticationResponse registerAccountant(RegisterRequestAccountant request) {
        var user = AccountantModel.builder()
                .userCI(request.getUserCI())
                .email(request.getEmail())
                .name(request.getName())
                .lastname(request.getLastname())
                .number(request.getNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ACCOUNTANT)
                .build();
        accountantRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticateAccountant(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getCi(),request.getPassword()));
        var user = accountantRepository.findByUsername(request.getCi()).orElseThrow(()-> new UserNotFoundException(String.format("The username %s not found in the accountant list",request.getCi())));
        revokeAllAccountantToken(user);
        var jwtToken = jwtService.generateToken(user);
        var token = TokenModel.builder()
                .accountant_id(user.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoke(false)
                .build();
        tokenRepository.save(token);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse registerClient(RegisterRequestClient request) {
        AccountantModel accountant = accountantRepository.findByUsername(request.getAccountCI()).orElseThrow(()-> new ResourceNotFoundException("Accountant not found"));
        var user = ClientModel.builder()
                .userCI(request.getUserCI())
                .email(request.getEmail())
                .name(request.getName())
                .lastname(request.getLastname())
                .number(request.getNumber())
                .password(passwordEncoder.encode(request.getUserCI()))
                .role(Role.CLIENT)
                .accountant_id(accountant.getId())
                .build();
        clientRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticateClient(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getCi(),request.getPassword()));
        var user = clientRepository.findByUsername(request.getCi()).orElseThrow(()-> new UserNotFoundException(String.format("The username %s not found in the client list",request.getCi())));
        revokeAllClientToken(user);
        var jwtToken = jwtService.generateToken(user);
        var token = TokenModel.builder()
                .client_id(user.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoke(false)
                .build();
        tokenRepository.save(token);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    private void revokeAllClientToken (ClientModel clientModel){
        var validClientToken = tokenRepository.findAllValidTokensByUser(clientModel.getId());
        if (validClientToken.isEmpty()){
            return;
        }
        validClientToken.forEach(f -> {
            f.setRevoke(true);
            f.setExpired(true);
        });
        tokenRepository.saveAll(validClientToken);
    }

    private void revokeAllAccountantToken (AccountantModel accountantModel){
        var validAccountantToken = tokenRepository.findAllValidTokensByUser(accountantModel.getId());
        if (validAccountantToken.isEmpty()){
            return;
        }
        validAccountantToken.forEach(f -> {
            f.setRevoke(true);
            f.setExpired(true);
        });
        tokenRepository.saveAll(validAccountantToken);
    }

    public AuthenticationResponse login(LoginRequest loginRequest){
        String role = loginRequest.getRole().name();

        if (role.equals(Role.ACCOUNTANT.name())){
            AuthenticationRequest authenticationRequest = new AuthenticationRequest();
            authenticationRequest.setCi(loginRequest.getCi());
            authenticationRequest.setPassword(loginRequest.getPassword());
            return authenticateAccountant(authenticationRequest);
        }
        if (role.equals(Role.CLIENT.name())){
            AuthenticationRequest authenticationRequest = new AuthenticationRequest();
            authenticationRequest.setCi(loginRequest.getCi());
            authenticationRequest.setPassword(loginRequest.getPassword());
            return authenticateClient(authenticationRequest);
        }

        throw new RuntimeException();
    }
}
