package com.example.ContaGest.service;

import com.example.ContaGest.model.*;
import com.example.ContaGest.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final EmailService emailService;


    public void revokeAllClientTokenButThis(ClientModel user, String token) {
        var validClientToken = tokenRepository.findAllValidTokenByClientId(user.getId());
        if (validClientToken.isEmpty()){
            return;
        }
        validClientToken.forEach(f -> {
            if (!f.getToken().equals(token)){
                f.setRevoke(true);
                f.setExpired(true);
            }
        });
        tokenRepository.saveAll(validClientToken);
    }
    public void revokeAllAccountantTokenButThis(AccountantModel user, String token) {
        var validAccountantToken = tokenRepository.findAllValidTokenByAccountantId(user.getId());
        if (validAccountantToken.isEmpty()){
            return;
        }
        validAccountantToken.forEach(f -> {
            if (!f.getToken().equals(token)){
                f.setRevoke(true);
                f.setExpired(true);
            }
        });
        tokenRepository.saveAll(validAccountantToken);

    }
    public void revokeAllAccountantToken (AccountantModel accountantModel){
        var validAccountantToken = tokenRepository.findAllValidTokenByAccountantId(accountantModel.getId());
        if (validAccountantToken.isEmpty()){
            return;
        }
        validAccountantToken.forEach(f -> {
            f.setRevoke(true);
            f.setExpired(true);
        });
        tokenRepository.saveAll(validAccountantToken);
    }
    public void revokeAllClientToken (ClientModel clientModel){
        var validClientToken = tokenRepository.findAllValidTokenByClientId(clientModel.getId());
        if (validClientToken.isEmpty()){
            return;
        }
        validClientToken.forEach(f -> {
            f.setRevoke(true);
            f.setExpired(true);
        });
        tokenRepository.saveAll(validClientToken);
    }
    public void GenerateTokenAndSendEmailChangeEmailClient(ClientModel clientModel, String newEmail) {
        String jwtToken = jwtService.generateToken(clientModel, Token.CHANGE_EMAIL);
        var token = TokenModel.builder()
                .client_id(clientModel.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .tokenFormat(Token.CHANGE_EMAIL)
                .email(newEmail)
                .build();
        tokenRepository.save(token);
        String link = "http://localhost:8080/api/v1/auth/confirm-change-email?token=" + jwtToken;
        emailService.send(newEmail,emailService.buildEmail(clientModel.getName(),link));
    }
    public void GenerateTokenAndSendEmailChangeEmailAccountant(AccountantModel accountant, String newEmail) {
        String jwtToken = jwtService.generateToken(accountant, Token.CHANGE_EMAIL);
        var token = TokenModel.builder()
                .accountant_id(accountant.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .tokenFormat(Token.CHANGE_EMAIL)
                .email(newEmail)
                .build();
        tokenRepository.save(token);
        String link = "http://localhost:8080/api/v1/auth/confirm-change-email?token=" + jwtToken;
        emailService.send(newEmail,emailService.buildEmail(accountant.getName(),link));
    }
    public void GenerateTokenAndSendEmailRegisterClient(ClientModel client){
        var jwtToken = jwtService.generateToken(client,Token.REGISTRATION);
        var token = TokenModel.builder()
                .client_id(client.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .tokenFormat(Token.REGISTRATION)
                .email(client.getEmail())
                .build();
        tokenRepository.save(token);
        String link = "http://localhost:8080/api/v1/auth/confirm-client?token=" + jwtToken;
        emailService.send(client.getEmail(),emailService.buildEmail(client.getName(),link));
    }
    public void GenerateTokenAndSendEmailRegisterAccountant(AccountantModel accountant){
        var jwtToken = jwtService.generateToken(accountant,Token.REGISTRATION);
        var token = TokenModel.builder()
                .accountant_id(accountant.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .tokenFormat(Token.REGISTRATION)
                .email(accountant.getEmail())
                .build();
        tokenRepository.save(token);
        String link = "http://localhost:8080/api/v1/auth/confirm-accountant?token=" + jwtToken;
        emailService.send(accountant.getEmail(),emailService.buildEmail(accountant.getName(),link));
    }
    public void GenerateTokenAndSendEmailForgotPasswordAccountant(AccountantModel accountant){
        var jwtToken = jwtService.generateToken(accountant,Token.FORGOT_PASSWORD);
        var token = TokenModel.builder()
                .accountant_id(accountant.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .tokenFormat(Token.FORGOT_PASSWORD)
                .email(accountant.getEmail())
                .build();
        tokenRepository.save(token);
        String link = "http://localhost:8080/api/v1/password/confirm-forgot-password?token=" + jwtToken;
        emailService.send(accountant.getEmail(),emailService.buildEmail(accountant.getName(),link));
    }
    public void GenerateTokenAndSendEmailForgotPasswordClient(ClientModel client){
        var jwtToken = jwtService.generateToken(client,Token.FORGOT_PASSWORD);
        var token = TokenModel.builder()
                .client_id(client.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .tokenFormat(Token.FORGOT_PASSWORD)
                .email(client.getEmail())
                .build();
        tokenRepository.save(token);
        String link = "http://localhost:8080/api/v1/password/confirm-forgot-password?token=" + jwtToken;
        emailService.send(client.getEmail(),emailService.buildEmail(client.getName(),link));
    }
}
