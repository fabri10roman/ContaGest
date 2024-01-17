package com.example.ContaGest.service;


import com.example.ContaGest.dto.*;
import com.example.ContaGest.exception.AccountNotVerifiedException;
import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.exception.UserAlreadyExistsException;
import com.example.ContaGest.model.*;
import com.example.ContaGest.repository.AccountantRepository;
import com.example.ContaGest.repository.ClientRepository;
import com.example.ContaGest.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.ContaGest.exception.UserNotFoundException;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

import static com.example.ContaGest.service.PasswordService.generateRandomPassword;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AccountantRepository accountantRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    public AuthenticationResponse registerAccountant(RegisterRequestAccountant request) {

        Optional<AccountantModel> accountantModel = accountantRepository.findByUsername(request.getUserCI());

        if(accountantModel.isPresent()){
            AccountantModel accountant = accountantModel.get();
            if (!accountant.isConfirmed()){
                if (accountant.getEmail().equals(request.getEmail()) && accountant.getName().equals(request.getName())
                        && accountant.getLastname().equals(request.getLastname()) && accountant.getCi().equals(request.getUserCI())
                        && accountant.getPhoneNumber().equals(request.getNumber())
                        && passwordEncoder.matches(request.getPassword(),accountant.getPassword())
                ){
                    revokeAllAccountantToken(accountant);
                    String jwtToken = GenerateTokenAndSendEmailRegisterAccountant(accountant);
                    return AuthenticationResponse.builder()
                            .token(jwtToken)
                            .build();
                }
            }
            throw new UserAlreadyExistsException(String.format("Accountant with username %s already taken",request.getUserCI()));
        }
        var user = AccountantModel.builder()
                .ci(request.getUserCI())
                .email(request.getEmail())
                .name(request.getName())
                .lastname(request.getLastname())
                .phoneNumber(request.getNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ACCOUNTANT)
                .isEnable(false)
                .isConfirmed(false)
                .build();
        accountantRepository.save(user);
        String jwtToken = GenerateTokenAndSendEmailRegisterAccountant(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    private String GenerateTokenAndSendEmailRegisterAccountant(AccountantModel accountant){
        var jwtToken = jwtService.generateTokenRegistrationAccountant(accountant);
        var token = TokenModel.builder()
                .accountant_id(accountant.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .isRegistration(true)
                .build();
        tokenRepository.save(token);
        String link = "http://localhost:8080/api/v1/auth/confirm-accountant?token=" + jwtToken;
        emailService.send(accountant.getEmail(),emailService.buildEmail(accountant.getName(),link));
        return jwtToken;
    }

    @Transactional
    public ResponseEntity<?> confirmTokenAccountant (String token) {
        TokenModel tokenModel = tokenRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        if (!tokenModel.isRegistration()) {
            throw new UserAlreadyExistsException("Email already confirmed");
        }
        if (tokenModel.isExpired() && tokenModel.isRevoke()) {
            throw new IllegalStateException("Token expired");
        }
        if (jwtService.isTokenExpired(token)){
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            tokenRepository.save(tokenModel);
            throw new IllegalStateException("Token expired");
        }
        String username = jwtService.getUsername(token);
        AccountantModel accountantModel = accountantRepository.findByUsername(username).
                orElseThrow(() -> new UserNotFoundException(String.format("User with username %s not found",username)));
        accountantModel.setEnable(true);
        accountantModel.setConfirmed(true);
        tokenModel.setRevoke(true);
        tokenModel.setExpired(true);
        accountantRepository.save(accountantModel);
        return ResponseEntity.ok("Confirmed");
    }

    private AuthenticationResponse authenticateAccountant(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getCi(),request.getPassword()));
        var user = accountantRepository.findByUsername(request.getCi()).
                orElseThrow(()-> new UserNotFoundException(String.format("The username %s not found in the accountant list",request.getCi())));
        if (!user.isConfirmed()){
            throw new AccountNotVerifiedException(String.format("The email %s is not confirmed",user.getEmail()));
        }
        revokeAllAccountantToken(user);
        var jwtToken = jwtService.generateToken(user);
        var token = TokenModel.builder()
                .accountant_id(user.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .isRegistration(false)
                .build();
        tokenRepository.save(token);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public String registerClient(RegisterRequestClient request) {

        Optional<ClientModel> clientModel = clientRepository.findByUsername(request.getUserCI());

        if(clientModel.isPresent()){
            ClientModel client = clientModel.get();
            if (!client.isConfirmed()){
                if (client.getEmail().equals(request.getEmail()) && client.getName().equals(request.getName())
                        && client.getLastname().equals(request.getLastname()) && client.getCi().equals(request.getUserCI())
                        && client.getPhoneNumber().equals(request.getNumber())
                ){
                    revokeAllClientToken(client);
                    String jwtToken = GenerateTokenAndSendEmailRegisterClient(client);
                    return "Check your email";
                }
            }
            throw new UserAlreadyExistsException(String.format("Accountant with username %s already taken",request.getUserCI()));
        }
        String token = request.getToken();
        String accountantUsername = jwtService.getUsername(token);
        AccountantModel accountant = accountantRepository.findByUsername(accountantUsername)
                .orElseThrow(()->new UserNotFoundException(String.format("Accountant with username %s not found",accountantUsername)));
        String pw = request.getUserCI() + "_" + generateRandomPassword();
        var user = ClientModel.builder()
                .ci(request.getUserCI())
                .email(request.getEmail())
                .name(request.getName())
                .lastname(request.getLastname())
                .phoneNumber(request.getNumber())
                .password(passwordEncoder.encode(pw))
                .role(Role.CLIENT)
                .isEnable(false)
                .isConfirmed(false)
                .accountant_id(accountant.getId())
                .build();
        clientRepository.save(user);
        String jwtToken = GenerateTokenAndSendEmailRegisterClient(user);
        return pw;
    }

    private String GenerateTokenAndSendEmailRegisterClient(ClientModel client){
        var jwtToken = jwtService.generateTokenRegistrationClient(client);
        var token = TokenModel.builder()
                .client_id(client.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .isRegistration(true)
                .build();
        tokenRepository.save(token);
        String link = "http://localhost:8080/api/v1/auth/confirm-client?token=" + jwtToken;
        emailService.send(client.getEmail(),emailService.buildEmail(client.getName(),link));
        return jwtToken;
    }

    @Transactional
    public ResponseEntity<?> confirmTokenClient (String token) {
        TokenModel tokenModel = tokenRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        if (!tokenModel.isRegistration()) {
            throw new UserAlreadyExistsException("Email already confirmed");
        }
        if (tokenModel.isExpired() && tokenModel.isRevoke()) {
            throw new IllegalStateException("Token expired");
        }
        if (jwtService.isTokenExpired(token)){
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            tokenRepository.save(tokenModel);
            throw new IllegalStateException("Token expired");
        }

        String username = jwtService.getUsername(token);
        ClientModel clientModel = clientRepository.findByUsername(username).
                orElseThrow(() -> new UserNotFoundException(String.format("User with username %s not found",username)));
        clientModel.setEnable(true);
        clientModel.setConfirmed(true);
        tokenModel.setRevoke(true);
        tokenModel.setExpired(true);
        clientRepository.save(clientModel);
        return ResponseEntity.ok("Confirmed");
    }

    private AuthenticationResponse authenticateClient(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getCi(),request.getPassword()));
        var user = clientRepository.findByUsername(request.getCi())
                .orElseThrow(()-> new UserNotFoundException(String.format("The username %s not found in the client list",request.getCi())));
        if (!user.isConfirmed()){
            throw new AccountNotVerifiedException(String.format("The email %s is not confirmed",user.getEmail()));
        }
        revokeAllClientToken(user);
        var jwtToken = jwtService.generateToken(user);
        var token = TokenModel.builder()
                .client_id(user.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .isRegistration(false)
                .build();
        tokenRepository.save(token);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public void revokeAllClientToken (ClientModel clientModel){
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

    public void revokeAllAccountantToken (AccountantModel accountantModel){
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
