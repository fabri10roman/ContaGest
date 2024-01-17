package com.example.ContaGest.service;

import com.example.ContaGest.dto.ChangePasswordRequest;
import com.example.ContaGest.dto.ForgotPasswordConfirmRequest;
import com.example.ContaGest.dto.ForgotPasswordRequest;
import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.exception.UserNotFoundException;
import com.example.ContaGest.model.*;
import com.example.ContaGest.repository.AccountantRepository;
import com.example.ContaGest.repository.ClientRepository;
import com.example.ContaGest.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

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
        String role = jwtService.getRole(jwt);

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

    }

    public String forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();
        String role = request.getRole();

        if (role.equals(Role.CLIENT.name())){
            ClientModel client = clientRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException(String.format("Client with %s email not found",email)));
            List<String> jwtToken = tokenRepository.findTokenRegisterClientByClientId(client.getId());
            if (jwtToken.isEmpty()){
                throw new ResourceNotFoundException(String.format("The token forgot password of the client with email %s not found",email));
            }
            Optional<TokenModel> tokenModel = tokenRepository.findTokenForgotPasswordClientByClientID(client.getId());
            if (tokenModel.isPresent()){
                throw new IllegalStateException("We already sent you a email to change you password");
            }
            return GenerateTokenAndSendEmailForgotPasswordClient(client);
        }
        AccountantModel accountant = accountantRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(String.format("Accountant with %s email not found",email)));
        List<String> jwtToken = tokenRepository.findTokenRegisterAccountantByAccountantId(accountant.getId());
        if (jwtToken.isEmpty()){
            throw new ResourceNotFoundException(String.format("The token forgot password of the accountant with email %s not found",email));
        }
        Optional<TokenModel> tokenModel = tokenRepository.findTokenForgotPasswordAccountantByAccountantID(accountant.getId());
        if (tokenModel.isPresent()){
            throw new IllegalStateException("We already sent you a email to change you password");
        }
        return GenerateTokenAndSendEmailForgotPasswordAccountant(accountant);
    }

    private String GenerateTokenAndSendEmailForgotPasswordAccountant(AccountantModel accountant){
        var jwtToken = jwtService.generateTokenForgotPassword(accountant);
        var token = TokenModel.builder()
                .accountant_id(accountant.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .isRegistration(false)
                .isForgotPassword(true)
                .build();
        tokenRepository.save(token);
        String link = "http://localhost:8080/api/v1/password/confirm-forgot-password?token=" + jwtToken;
        emailService.send(accountant.getEmail(),emailService.buildEmail(accountant.getName(),link));
        return link;
    }

    private String GenerateTokenAndSendEmailForgotPasswordClient(ClientModel client){
        var jwtToken = jwtService.generateTokenForgotPassword(client);
        var token = TokenModel.builder()
                .client_id(client.getId())
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoke(false)
                .isRegistration(false)
                .isForgotPassword(true)
                .build();
        tokenRepository.save(token);
        String link = "http://localhost:8080/api/v1/password/confirm-forgot-password?token=" + jwtToken;
        emailService.send(client.getEmail(),emailService.buildEmail(client.getName(),link));
        return link;
    }

    public ResponseEntity<?> confirmForgotPassword(String token, ForgotPasswordConfirmRequest request) {
        TokenModel tokenModel = tokenRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException("Token not found"));
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
        String role = jwtService.getRole(token);
        if (role.equals(Role.CLIENT.name())){
            ClientModel client = clientRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException(String.format("Client with %s username not found",username)));
            if (!request.getNewPassword().equals(request.getConfirmPassword())){
                throw new BadCredentialsException("Password are not the same");
            }
            client.setPassword(passwordEncoder.encode(request.getNewPassword()));
            clientRepository.save(client);
            authenticationService.revokeAllClientToken(client);
        }
        else {
            AccountantModel accountant = accountantRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException(String.format("Accountant with %s username not found",username)));
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
