package com.example.ContaGest.service;

import com.example.ContaGest.dto.ChangePasswordRequest;
import com.example.ContaGest.dto.ForgotPasswordConfirmRequest;
import com.example.ContaGest.dto.ForgotPasswordRequest;
import com.example.ContaGest.exception.ConflictExcepcion;
import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.exception.TokenExpiredException;
import com.example.ContaGest.model.*;
import com.example.ContaGest.repository.AccountantRepository;
import com.example.ContaGest.repository.ClientRepository;
import com.example.ContaGest.repository.TokenRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final ClientRepository clientRepository;
    private final AccountantRepository accountantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationService authenticationService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZÑabcdefghijklmnopqrstuvwxyzñ";

    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {
        String jwt = request.getToken();
        TokenModel tokenModel = tokenRepository.findByToken(jwt).orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        String role;
        try{
            role = jwtService.getRole(jwt);
        }catch (JwtException e){
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            tokenRepository.save(tokenModel);
            throw new TokenExpiredException();
        }
        if (tokenModel.isExpired() && tokenModel.isRevoke()) {
            throw new TokenExpiredException();
        }
        if (role.equals(Role.ACCOUNTANT.name())){
            changePasswordAccount(request,connectedUser);
        }else if (role.equals(Role.CLIENT.name())){
            changePasswordClient(request,connectedUser);
        }
    }

    public void changePasswordAccount(ChangePasswordRequest request, Principal connectedUser) {
        AccountantModel user = (AccountantModel) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("The current password does not match");
        }
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new BadCredentialsException("Password are not the same");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountantRepository.save(user);
        authenticationService.revokeAllAccountantTokenButThis(user,request.getToken());
    }

    public void changePasswordClient(ChangePasswordRequest request, Principal connectedUser) {
        ClientModel user = (ClientModel) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("The current password does not match");
        }
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new BadCredentialsException("Password are not the same");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        clientRepository.save(user);
        authenticationService.revokeAllClientTokenButThis(user,request.getToken());
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();
        String role = request.getRole();
        if (role.equals(Role.CLIENT.name())){
            ClientModel client = clientRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("Client with email %s not found",email)));
            List<TokenModel> tokenModel = tokenRepository.findTokenForgotPasswordClientByClientID(client.getId());
            CheckForgotToken(tokenModel);
            return GenerateTokenAndSendEmailForgotPasswordClient(client);
        }
        AccountantModel accountant = accountantRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Accountant with %s email not found",email)));
        List<TokenModel> tokenModel = tokenRepository.findTokenForgotPasswordAccountantByAccountantID(accountant.getId());
        CheckForgotToken(tokenModel);
        return GenerateTokenAndSendEmailForgotPasswordAccountant(accountant);
    }

    private void CheckForgotToken(List<TokenModel> tokenModel) {
        if (!tokenModel.isEmpty()){
            for (TokenModel token : tokenModel){
                try {
                    String jwtToken = token.getToken();
                    jwtService.isTokenExpired(jwtToken);
                    throw new ConflictExcepcion("Email already send before, please check your email");
                }catch (JwtException e){
                    token.setRevoke(true);
                    token.setExpired(true);
                    tokenRepository.save(token);
                }
            }
        }
    }

    private String GenerateTokenAndSendEmailForgotPasswordAccountant(AccountantModel accountant){
        var jwtToken = jwtService.generateToken(accountant,Token.FORGOT_PASSWORD);
        var token = TokenModel.builder()
                .accountant_id(accountant.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .tokenFormat(Token.FORGOT_PASSWORD)
                .build();
        tokenRepository.save(token);
        String link = "http://localhost:8080/api/v1/password/confirm-forgot-password?token=" + jwtToken;
        emailService.send(accountant.getEmail(),emailService.buildEmail(accountant.getName(),link));
        return link;
    }

    private String GenerateTokenAndSendEmailForgotPasswordClient(ClientModel client){
        var jwtToken = jwtService.generateToken(client,Token.FORGOT_PASSWORD);
        var token = TokenModel.builder()
                .client_id(client.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .tokenFormat(Token.FORGOT_PASSWORD)
                .build();
        tokenRepository.save(token);
        String link = "http://localhost:8080/api/v1/password/confirm-forgot-password?token=" + jwtToken;
        emailService.send(client.getEmail(),emailService.buildEmail(client.getName(),link));
        return link;
    }

    public ResponseEntity<?> confirmForgotPassword(String token, ForgotPasswordConfirmRequest request) {
        TokenModel tokenModel = tokenRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        String username;
        String role;
        if (tokenModel.isExpired() && tokenModel.isRevoke()) {
            throw new TokenExpiredException();
        }
        try{
            username = jwtService.getUsername(token);
            role = jwtService.getRole(token);
        }catch (JwtException e){
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            tokenRepository.save(tokenModel);
            throw new TokenExpiredException();
        }
        if (role.equals(Role.CLIENT.name())){
            ClientModel client = clientRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("Client with CI %s not found",username)));
            if (!request.getNewPassword().equals(request.getConfirmPassword())){
                throw new BadCredentialsException("Password are not the same");
            }
            client.setPassword(passwordEncoder.encode(request.getNewPassword()));
            clientRepository.save(client);
            authenticationService.revokeAllClientToken(client);
        }
        else {
            AccountantModel accountant = accountantRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("Accountant with CI %s not found",username)));
            if (!request.getNewPassword().equals(request.getConfirmPassword())){
                throw new BadCredentialsException("Password are not the same");
            }
            accountant.setPassword(passwordEncoder.encode(request.getNewPassword()));
            accountantRepository.save(accountant);
            authenticationService.revokeAllAccountantToken(accountant);
        }
        return ResponseEntity.ok("Confirmed");
    }

    public static String generateRandomPassword() {
        int length = 5;
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(ALLOWED_CHARACTERS.charAt(RANDOM.nextInt(ALLOWED_CHARACTERS.length())));
        }
        return password.toString();
    }
}
