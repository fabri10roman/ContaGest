package com.example.ContaGest.service;

import com.example.ContaGest.dto.request.ChangePasswordRequest;
import com.example.ContaGest.dto.request.ForgotPasswordConfirmRequest;
import com.example.ContaGest.dto.request.ForgotPasswordRequest;
import com.example.ContaGest.dto.ResponsePayload;
import com.example.ContaGest.exception.ConflictExcepcion;
import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.model.*;
import com.example.ContaGest.repository.AccountantRepository;
import com.example.ContaGest.repository.ClientRepository;
import com.example.ContaGest.repository.TokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

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
    private final TokenService tokenService;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZÑabcdefghijklmnopqrstuvwxyzñ";


    @Transactional
    public ResponsePayload changePassword(ChangePasswordRequest request) {
        String jwt = request.getToken();
        TokenModel tokenModel = tokenRepository.findByToken(jwt).orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        String role;
        String username = jwtService.getUsername(jwt);
        try{
            role = jwtService.getRole(jwt);
        }catch (ExpiredJwtException e){
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            tokenRepository.save(tokenModel);
            throw new ExpiredJwtException(null,null,null);
        }catch (SignatureException e) {
            throw new SignatureException(null);
        }
        if (tokenModel.isExpired() && tokenModel.isRevoke()) {
            throw new ExpiredJwtException(null,null,null);
        }
        if (role.equals(Role.ACCOUNTANT.name())){
            AccountantModel accountant = accountantRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("Accountant with CI %s not found",username)));
            String email = accountant.getEmail();
            changePasswordAccount(request,accountant);
            emailService.send(email,"Password changed. Your password has been changed, if you did not do this, please contact us");
        }else if (role.equals(Role.CLIENT.name())){
            ClientModel client = clientRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("Client with CI %s not found",username)));
            String email = client.getEmail();
            changePasswordClient(request,client);
            emailService.send(email,"Password changed. Your password has been changed, if you did not do this, please contact us");
        }
        return ResponsePayload.builder()
                .status(HttpStatus.OK.value())
                .title(HttpStatus.OK.getReasonPhrase())
                .message("Password changed successfully")
                .build();
    }

    private void changePasswordAccount(ChangePasswordRequest request, AccountantModel user) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("The current password does not match");
        }
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new BadCredentialsException("Password are not the same");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountantRepository.save(user);
        tokenService.revokeAllAccountantTokenButThis(user,request.getToken());
    }

    private void changePasswordClient(ChangePasswordRequest request, ClientModel user) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("The current password does not match");
        }
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new BadCredentialsException("Password are not the same");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        clientRepository.save(user);
        tokenService.revokeAllClientTokenButThis(user,request.getToken());
    }

    @Transactional
    public ResponsePayload forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();
        String role = request.getRole();
        if (role.equals(Role.CLIENT.name())){
            ClientModel client = clientRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("Client with email %s not found",email)));
            if (!client.isEnable()){
                throw new DisabledException(String.format("The client with email %s is disabled",email));
            }
            if (!client.isConfirmed()){
                throw new ConflictExcepcion(String.format("The email %s is not confirmed",email));
            }
            List<TokenModel> tokenModel = tokenRepository.findTokenForgotPasswordClientByClientID(client.getId());
            CheckForgotToken(tokenModel);
            tokenService.GenerateTokenAndSendEmailForgotPasswordClient(client);
            return ResponsePayload.builder()
                    .status(HttpStatus.OK.value())
                    .title(HttpStatus.OK.getReasonPhrase())
                    .message("Please check your email")
                    .build();
        }
        AccountantModel accountant = accountantRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Accountant with %s email not found",email)));
        if (!accountant.isEnable()){
            throw new DisabledException(String.format("The accountant with email %s is disabled",email));
        }
        if (!accountant.isConfirmed()){
            throw new ConflictExcepcion(String.format("The email %s is not confirmed",email));
        }
        List<TokenModel> tokenModel = tokenRepository.findTokenForgotPasswordAccountantByAccountantID(accountant.getId());
        CheckForgotToken(tokenModel);
        tokenService.GenerateTokenAndSendEmailForgotPasswordAccountant(accountant);
        return ResponsePayload.builder()
                .status(HttpStatus.OK.value())
                .title(HttpStatus.OK.getReasonPhrase())
                .message("Please check your email")
                .build();
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
    @Transactional
    public ResponsePayload confirmForgotPassword(String token, ForgotPasswordConfirmRequest request) {
        TokenModel tokenModel = tokenRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        String username;
        String role;
        if (tokenModel.isExpired() && tokenModel.isRevoke()) {
            throw new ExpiredJwtException(null,null,null);
        }
        try{
            username = jwtService.getUsername(token);
            role = jwtService.getRole(token);
        }catch (ExpiredJwtException e){
            tokenModel.setRevoke(true);
            tokenModel.setExpired(true);
            tokenRepository.save(tokenModel);
            throw new ExpiredJwtException(null,null,null);
        }catch (SignatureException e) {
            throw new SignatureException(null);
        }
        if (role.equals(Role.CLIENT.name())){
            ClientModel client = clientRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("Client with CI %s not found",username)));
            if (!request.getNewPassword().equals(request.getConfirmPassword())){
                throw new BadCredentialsException("Password are not the same");
            }
            client.setPassword(passwordEncoder.encode(request.getNewPassword()));
            clientRepository.save(client);
            tokenService.revokeAllClientToken(client);
        }
        else {
            AccountantModel accountant = accountantRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("Accountant with CI %s not found",username)));
            if (!request.getNewPassword().equals(request.getConfirmPassword())){
                throw new BadCredentialsException("Password are not the same");
            }
            accountant.setPassword(passwordEncoder.encode(request.getNewPassword()));
            accountantRepository.save(accountant);
            tokenService.revokeAllAccountantToken(accountant);
        }
        return ResponsePayload.builder()
                .status(HttpStatus.OK.value())
                .title(HttpStatus.OK.getReasonPhrase())
                .message("Password changed successfully")
                .build();
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
