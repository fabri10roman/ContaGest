package com.example.ContaGest.service;


import com.example.ContaGest.dto.ResponsePayload;
import com.example.ContaGest.dto.request.LoginRequest;
import com.example.ContaGest.dto.request.RegisterAccountantRequest;
import com.example.ContaGest.dto.request.RegisterClientRequest;
import com.example.ContaGest.dto.response.AuthenticationResponse;
import com.example.ContaGest.dto.request.AuthenticationRequest;
import com.example.ContaGest.exception.*;
import com.example.ContaGest.model.*;
import com.example.ContaGest.repository.AccountantRepository;
import com.example.ContaGest.repository.ClientRepository;
import com.example.ContaGest.repository.TokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collections;
import java.util.List;
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
    private final TokenService tokenService;

    @Transactional
    public ResponsePayload registerAccountant(RegisterAccountantRequest request) throws BadRequestException {
        Optional<AccountantModel> accountantModelByUsername = accountantRepository.findByUsername(request.getCi());
        if(emailService.isEmailNotValid(request.getEmail())){
            throw new BadRequestException(String.format("Email %s not valid",request.getEmail()));
        }
        if(accountantModelByUsername.isPresent()){
            AccountantModel accountant = accountantModelByUsername.get();
            if (!accountant.isConfirmed()){
                if (!request.getPassword().equals(request.getConfirmPassword())){
                    throw new BadRequestException("The password and the confirmation password do not match");
                }
                if (accountant.getEmail().equals(request.getEmail()) && accountant.getName().equals(request.getName())
                        && accountant.getLastname().equals(request.getLastname()) && accountant.getCi().equals(request.getCi())
                        && accountant.getPhoneNumber().equals(request.getPhoneNumber())
                        && passwordEncoder.matches(request.getPassword(),accountant.getPassword())
                ){
                    tokenService.revokeAllAccountantToken(accountant);
                    tokenService.GenerateTokenAndSendEmailRegisterAccountant(accountant);
                    return ResponsePayload.builder()
                            .status(HttpStatus.CREATED.value())
                            .title(HttpStatus.CREATED.getReasonPhrase())
                            .message("Registration successful, please confirm your email")
                            .build();
                }
                throw new BadRequestException("All fields must be the same as the first time you registered");
            }
            throw new ConflictExcepcion(String.format("Accountant with CI %s already registered",request.getCi()));
        }
        Optional<AccountantModel> accountantModelByEmail = accountantRepository.findByEmail(request.getEmail());
        if (accountantModelByEmail.isPresent()){
            throw new ConflictExcepcion(String.format("Email %s already taken", request.getEmail()));
        }
        if (!request.getPassword().equals(request.getConfirmPassword())){
            throw new BadRequestException("The password and the confirmation password do not match");
        }
        var user = AccountantModel.builder()
                .ci(request.getCi())
                .email(request.getEmail())
                .name(request.getName())
                .lastname(request.getLastname())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ACCOUNTANT)
                .isEnable(false)
                .isConfirmed(false)
                .build();
        accountantRepository.save(user);
        tokenService.GenerateTokenAndSendEmailRegisterAccountant(user);
        return ResponsePayload.builder()
                .status(HttpStatus.CREATED.value())
                .title(HttpStatus.CREATED.getReasonPhrase())
                .message("Registration successful, please confirm your email")
                .build();
    }
    @Transactional
    public ResponsePayload confirmTokenRegistration (String token) {
        TokenModel tokenModel = tokenRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        Integer id;
        String role;
        try{
            id = jwtService.getId(token);
            role = jwtService.getRole(token);
        }catch (ExpiredJwtException e){
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            tokenRepository.save(tokenModel);
            throw new ExpiredJwtException(null,null,null);
        }catch (SignatureException e){
            throw new SignatureException(null);
        }
        if (!tokenModel.getTokenFormat().name().equals(Token.REGISTRATION.name())) {
            throw new IllegalStateException("The token is not for registration");
        }
        if (tokenModel.isExpired() && tokenModel.isRevoke()) {
            throw new ExpiredJwtException(null,null,null);
        }
        if(role.equals(Role.ACCOUNTANT.name())){
            AccountantModel accountantModel = accountantRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("Accountant with ID %s not found",id)));
            accountantModel.setEnable(true);
            accountantModel.setConfirmed(true);
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            tokenRepository.save(tokenModel);
            accountantRepository.save(accountantModel);
            return ResponsePayload.builder()
                    .status(HttpStatus.OK.value())
                    .title(HttpStatus.OK.getReasonPhrase())
                    .message("Confirmed")
                    .build();
        }else if (role.equals(Role.CLIENT.name())){
            ClientModel clientModel = clientRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("Client with ID %s not found",id)));
            clientModel.setEnable(true);
            clientModel.setConfirmed(true);
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            tokenRepository.save(tokenModel);
            clientRepository.save(clientModel);
            return ResponsePayload.builder()
                    .status(HttpStatus.OK.value())
                    .title(HttpStatus.OK.getReasonPhrase())
                    .message("Confirmed")
                    .build();
        }
        throw new IllegalStateException("Something went wrong with the confirmation of the token");
    }
    private ResponsePayload authenticateAccountant(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getCi(), request.getPassword()));
        } catch (DisabledException e) {
            throw new DisabledException(String.format("The accountant with CI %s is not enable", request.getCi()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Bad credentials");
        } catch (LockedException e) {
            throw new LockedException(String.format("The accountant with CI %s is locked", request.getCi()));
        } catch (Exception e) {
            throw new IllegalStateException("Something went wrong with the authentication");
        }
        var user = accountantRepository.findByUsername(request.getCi()).
                orElseThrow(()-> new UsernameNotFoundException(String.format("The accountant with CI %s not found",request.getCi())));
        if (!user.isConfirmed()){
            throw new ConflictExcepcion(String.format("The email %s is not confirmed",user.getEmail()));
        }
        tokenService.revokeAllAccountantToken(user);
        var jwtToken = jwtService.generateToken(user,Token.LOGIN);
        var token = TokenModel.builder()
                .accountant_id(user.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .tokenFormat(Token.LOGIN)
                .email(user.getEmail())
                .build();
        tokenRepository.save(token);
        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .token(jwtToken)
                .CI(user.getCi())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .id(user.getId())
                .lastname(user.getLastname())
                .phoneNumber(user.getPhoneNumber())
                .build();
        List<Object> data = Collections.singletonList(authenticationResponse);
        return ResponsePayload.builder()
                .status(HttpStatus.OK.value())
                .title(HttpStatus.OK.getReasonPhrase())
                .message("Login successful")
                .data(data)
                .build();
    }

    @Transactional
    public ResponsePayload registerClient(RegisterClientRequest request) throws BadRequestException {
        if (emailService.isEmailNotValid(request.getEmail())){
            throw new BadRequestException(String.format("Email %s not valid",request.getEmail()));
        }
        String accountantUsername;
        String token = request.getToken();
        TokenModel tokenModel = tokenRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        try {
            accountantUsername = jwtService.getUsername(token);
        }catch (ExpiredJwtException e){
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            throw new ExpiredJwtException(null,null,null);
        }catch (SignatureException e){
            throw new SignatureException(null);
        }
        if (tokenModel.isExpired() && tokenModel.isRevoke()) {
            throw new ExpiredJwtException(null,null,null);
        }
        Optional<ClientModel> clientModelByUsername = clientRepository.findByUsername(request.getCi());
        if(clientModelByUsername.isPresent()){
            ClientModel client = clientModelByUsername.get();
            if (!client.isConfirmed()){
                if (client.getEmail().equals(request.getEmail()) && client.getName().equals(request.getName())
                        && client.getLastname().equals(request.getLastname()) && client.getCi().equals(request.getCi())
                        && client.getPhoneNumber().equals(request.getPhoneNumber())
                ){
                    tokenService.revokeAllClientToken(client);
                    String pw = request.getCi() + "_" + generateRandomPassword();
                    client.setPassword(passwordEncoder.encode(pw));
                    clientRepository.save(client);
                    tokenService.GenerateTokenAndSendEmailRegisterClient(client);
                    List<Object> data = Collections.singletonList(pw);
                    return ResponsePayload.builder()
                            .status(HttpStatus.CREATED.value())
                            .title(HttpStatus.CREATED.getReasonPhrase())
                            .message("Registration successful")
                            .data(data)
                            .build();
                }
                throw new BadRequestException("All fields must be the same as the first time you registered this user");
            }
            throw new ConflictExcepcion(String.format("Client with CI %s already registered",request.getCi()));
        }
        Optional<ClientModel> clientModelByEmail = clientRepository.findByEmail(request.getEmail());
        if (clientModelByEmail.isPresent()){
            throw new ConflictExcepcion(String.format("Email %s already taken", request.getEmail()));
        }
        AccountantModel accountant = accountantRepository.findByUsername(accountantUsername)
                .orElseThrow(()->new UsernameNotFoundException(String.format("Accountant with username %s not found",accountantUsername)));
        String pw = request.getCi() + "_" + generateRandomPassword();
        var user = ClientModel.builder()
                .ci(request.getCi())
                .email(request.getEmail())
                .name(request.getName())
                .lastname(request.getLastname())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(pw))
                .role(Role.CLIENT)
                .isEnable(false)
                .isConfirmed(false)
                .accountant_id(accountant.getId())
                .build();
        clientRepository.save(user);
        tokenService.GenerateTokenAndSendEmailRegisterClient(user);
        List<Object> data = Collections.singletonList(pw);
        return ResponsePayload.builder()
                .status(HttpStatus.CREATED.value())
                .title(HttpStatus.CREATED.getReasonPhrase())
                .message("Registration successful")
                .data(data)
                .build();
    }
    private ResponsePayload authenticateClient(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getCi(),request.getPassword()));
        } catch (DisabledException e) {
            throw new DisabledException(String.format("The client with CI %s is not enable", request.getCi()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Bad credentials");
        } catch (LockedException e) {
            throw new LockedException(String.format("The client with CI %s is locked", request.getCi()));
        } catch (Exception e) {
            throw new IllegalStateException("Something went wrong with the authentication");
        }
        var user = clientRepository.findByUsername(request.getCi())
                .orElseThrow(()-> new UsernameNotFoundException(String.format("The client with CI %s not found",request.getCi())));
        if (!user.isConfirmed()){
            throw new ConflictExcepcion(String.format("The email %s is not confirmed",user.getEmail()));
        }
        tokenService.revokeAllClientToken(user);
        var jwtToken = jwtService.generateToken(user,Token.LOGIN);
        var token = TokenModel.builder()
                .client_id(user.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .tokenFormat(Token.LOGIN)
                .email(user.getEmail())
                .build();
        tokenRepository.save(token);
        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .token(jwtToken)
                .CI(user.getCi())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .id(user.getId())
                .lastname(user.getLastname())
                .phoneNumber(user.getPhoneNumber())
                .build();
        List<Object> data = Collections.singletonList(authenticationResponse);
        return ResponsePayload.builder()
                .status(HttpStatus.OK.value())
                .title(HttpStatus.OK.getReasonPhrase())
                .message("Login successful")
                .data(data)
                .build();
    }
    public ResponsePayload login(LoginRequest loginRequest){
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
    @Transactional
    public ResponsePayload confirmChangeEmail(String token) {
        TokenModel tokenModel = tokenRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        String email = tokenModel.getEmail();
        Integer id;
        String role;
        try{
            id = jwtService.getId(token);
            role = jwtService.getRole(token);
        }catch (ExpiredJwtException e){
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            tokenRepository.save(tokenModel);
            if (tokenModel.getTokenFormat().name().equals(Token.CHANGE_EMAIL.name())) {
                if (tokenModel.getClient_id() != null) {
                    ClientModel clientModel = clientRepository.findById(tokenModel.getClient_id())
                            .orElseThrow(() -> new UsernameNotFoundException(String.format("Client with ID %s not found", tokenModel.getClient_id())));
                    if (!email.equals(clientModel.getEmail())){
                        throw new ExpiredJwtException(null,null,"Token expired");
                    }
                    if (!clientModel.isConfirmed()) {
                        tokenService.GenerateTokenAndSendEmailChangeEmailClient(clientModel, clientModel.getEmail());
                        throw new ExpiredJwtException(null,null,"Token expired. New email send");
                    }else {
                        throw new ConflictExcepcion(String.format("The email %s is already confirmed",clientModel.getEmail()));
                    }
                } else if (tokenModel.getAccountant_id() != null) {
                    AccountantModel accountantModel = accountantRepository.findById(tokenModel.getAccountant_id())
                            .orElseThrow(() -> new UsernameNotFoundException(String.format("Accountant with ID %s not found", tokenModel.getAccountant_id())));
                    if (!email.equals(accountantModel.getEmail())){
                        throw new ExpiredJwtException(null,null,"Token expired");
                    }
                    if (!accountantModel.isConfirmed()) {
                        tokenService.GenerateTokenAndSendEmailChangeEmailAccountant(accountantModel, accountantModel.getEmail());
                        throw new ExpiredJwtException(null,null,"Token expired. New email send");
                    } else {
                        throw new ConflictExcepcion(String.format("The email %s is already confirmed",accountantModel.getEmail()));
                    }
                } else {
                    throw new IllegalStateException("The token does not belong to any accountant or client");
                }
            }else {
                throw new IllegalStateException("The token is not for change email");
            }
        }catch (SignatureException e){
            throw new SignatureException(null);
        }
        if (tokenModel.isExpired() && tokenModel.isRevoke()) {
            throw new ExpiredJwtException(null,null,null);
        }
        if (!tokenModel.getTokenFormat().name().equals(Token.CHANGE_EMAIL.name())) {
            throw new IllegalStateException("The token is not for change email");
        }
        if(role.equals(Role.ACCOUNTANT.name())){
            AccountantModel accountantModel = accountantRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("Accountant with ID %s not found",id)));
            if (accountantModel.isConfirmed()){
                throw new ConflictExcepcion(String.format("The email %s is already confirmed",accountantModel.getEmail()));
            }
            accountantModel.setConfirmed(true);
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            tokenRepository.save(tokenModel);
            accountantRepository.save(accountantModel);
            return ResponsePayload.builder()
                    .status(HttpStatus.OK.value())
                    .title(HttpStatus.OK.getReasonPhrase())
                    .message("Confirmed")
                    .build();
        }else if (role.equals(Role.CLIENT.name())){
            ClientModel clientModel = clientRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("Client with ID %s not found",id)));
            if (clientModel.isConfirmed()){
                throw new ConflictExcepcion(String.format("The email %s is already confirmed",clientModel.getEmail()));
            }
            clientModel.setConfirmed(true);
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            tokenRepository.save(tokenModel);
            clientRepository.save(clientModel);
            return ResponsePayload.builder()
                    .status(HttpStatus.OK.value())
                    .title(HttpStatus.OK.getReasonPhrase())
                    .message("Confirmed")
                    .build();
        }
        throw new IllegalStateException("Something went wrong with the confirmation of the token");
    }
}
