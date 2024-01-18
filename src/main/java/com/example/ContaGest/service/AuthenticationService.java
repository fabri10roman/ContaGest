package com.example.ContaGest.service;


import com.example.ContaGest.dto.*;
import com.example.ContaGest.exception.*;
import com.example.ContaGest.model.*;
import com.example.ContaGest.repository.AccountantRepository;
import com.example.ContaGest.repository.ClientRepository;
import com.example.ContaGest.repository.TokenRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
    public AuthenticationResponse registerAccountant(RegisterRequestAccountant request) throws BadRequestException {
        Optional<AccountantModel> accountantModel = accountantRepository.findByUsername(request.getUserCI());
        if(validateEmail(request.getEmail())){
            throw new BadRequestException(String.format("Email %s not valid",request.getEmail()));
        }
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
                throw new BadRequestException("All fields must be the same as the first time you registered");
            }
            throw new ConflictExcepcion(String.format("Accountant with CI %s already taken",request.getUserCI()));
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

    private boolean validateEmail(String email) {
        return true;
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
        String username;
        try{
            username = jwtService.getUsername(token);
        }catch (JwtException e){
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            tokenRepository.save(tokenModel);
            throw new TokenExpiredException();
        }
        if (!tokenModel.isRegistration()) {
            throw new IllegalStateException("The token is not for registration");
        }
        if (tokenModel.isExpired() && tokenModel.isRevoke()) {
            throw new TokenExpiredException();
        }
        AccountantModel accountantModel = accountantRepository.findByUsername(username).
                orElseThrow(() -> new UsernameNotFoundException(String.format("Accountant with CI %s not found",username)));
        accountantModel.setEnable(true);
        accountantModel.setConfirmed(true);
        tokenModel.setRevoke(true);
        tokenModel.setExpired(true);
        tokenRepository.save(tokenModel);
        accountantRepository.save(accountantModel);
        return ResponseEntity.ok("Confirmed");
    }

    private AuthenticationResponse authenticateAccountant(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getCi(), request.getPassword()));
        } catch (DisabledException e) {
            throw new UserNotEnableExcepcion(String.format("The accountant with CI %s is not enable", request.getCi()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(String.format("Password incorrect for accountant with CI %s", request.getCi()));
        } catch (LockedException e) {
            throw new UserNotEnableExcepcion(String.format("The accountant with CI %s is locked", request.getCi()));
        } catch (Exception e) {
            throw new IllegalStateException("Something went wrong with the authentication");
        }
        var user = accountantRepository.findByUsername(request.getCi()).
                orElseThrow(()-> new UsernameNotFoundException(String.format("The accountant with CI %s not found",request.getCi())));
        if (!user.isConfirmed()){
            throw new UserNotEnableExcepcion(String.format("The email %s is not confirmed",user.getEmail()));
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

    public String registerClient(RegisterRequestClient request) throws BadRequestException {
        if (validateEmail(request.getEmail())){
            throw new BadRequestException(String.format("Email %s not valid",request.getEmail()));
        }
        String accountantUsername;
        String token = request.getToken();
        TokenModel tokenModel = tokenRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        try {
            accountantUsername = jwtService.getUsername(token);
        }catch (JwtException e){
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            throw new TokenExpiredException();
        }
        if (tokenModel.isExpired() && tokenModel.isRevoke()) {
            throw new TokenExpiredException();
        }
        Optional<ClientModel> clientModel = clientRepository.findByUsername(request.getUserCI());
        if(clientModel.isPresent()){
            ClientModel client = clientModel.get();
            if (!client.isConfirmed()){
                if (client.getEmail().equals(request.getEmail()) && client.getName().equals(request.getName())
                        && client.getLastname().equals(request.getLastname()) && client.getCi().equals(request.getUserCI())
                        && client.getPhoneNumber().equals(request.getNumber())
                ){
                    revokeAllClientToken(client);
                    String pw = request.getUserCI() + "_" + generateRandomPassword();
                    client.setPassword(passwordEncoder.encode(pw));
                    clientRepository.save(client);
                    String jwtToken = GenerateTokenAndSendEmailRegisterClient(client);
                    return pw;
                }
                throw new BadRequestException("All fields must be the same as the first time you registered this user");
            }
            throw new ConflictExcepcion(String.format("Client with CI %s already taken",request.getUserCI()));
        }
        AccountantModel accountant = accountantRepository.findByUsername(accountantUsername)
                .orElseThrow(()->new UsernameNotFoundException(String.format("Accountant with username %s not found",accountantUsername)));
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
        String username;
        try{
            username = jwtService.getUsername(token);
        }catch (JwtException e){
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            tokenRepository.save(tokenModel);
            throw new TokenExpiredException();
        }
        if (!tokenModel.isRegistration()) {
            throw new IllegalStateException("The token is not for registration");
        }
        if (tokenModel.isExpired() && tokenModel.isRevoke()) {
            throw new TokenExpiredException();
        }
        ClientModel clientModel = clientRepository.findByUsername(username).
                orElseThrow(() -> new UsernameNotFoundException(String.format("Client with CI %s not found",username)));
        clientModel.setEnable(true);
        clientModel.setConfirmed(true);
        tokenModel.setRevoke(true);
        tokenModel.setExpired(true);
        tokenRepository.save(tokenModel);
        clientRepository.save(clientModel);
        return ResponseEntity.ok("Confirmed");
    }

    private AuthenticationResponse authenticateClient(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getCi(),request.getPassword()));
        } catch (DisabledException e) {
            throw new UserNotEnableExcepcion(String.format("The client with CI %s is not enable", request.getCi()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(String.format("Password incorrect for client with CI %s", request.getCi()));
        } catch (LockedException e) {
            throw new UserNotEnableExcepcion(String.format("The client with CI %s is locked", request.getCi()));
        } catch (Exception e) {
            throw new IllegalStateException("Something went wrong with the authentication");
        }
        var user = clientRepository.findByUsername(request.getCi())
                .orElseThrow(()-> new UsernameNotFoundException(String.format("The client with CI %s not found",request.getCi())));
        if (!user.isConfirmed()){
            throw new ConflictExcepcion(String.format("The email %s is not confirmed",user.getEmail()));
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
        throw new IllegalStateException("Something went wrong with the authentication");
    }
}
