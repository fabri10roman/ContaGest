package com.example.ContaGest.service;


import com.example.ContaGest.dto.*;
import com.example.ContaGest.email.EmailSender;
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


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AccountantRepository accountantRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final EmailSender emailSender;
    public AuthenticationResponse registerAccountant(RegisterRequestAccountant request) {

        Optional<AccountantModel> accountantModel = accountantRepository.findByUsername(request.getUserCI());

        if(accountantModel.isPresent()){
            AccountantModel accountant = accountantModel.get();
            if (!accountant.isConfirmed()){
                if (accountant.getEmail().equals(request.getEmail()) && accountant.getName().equals(request.getName())
                        && accountant.getLastname().equals(request.getLastname()) && accountant.getUserCI().equals(request.getUserCI())
                        && accountant.getNumber().equals(request.getNumber())
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
                .userCI(request.getUserCI())
                .email(request.getEmail())
                .name(request.getName())
                .lastname(request.getLastname())
                .number(request.getNumber())
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
        emailSender.send(accountant.getEmail(),buildEmail(accountant.getName(),link));
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

    public AuthenticationResponse registerClient(RegisterRequestClient request) {

        Optional<ClientModel> clientModel = clientRepository.findByUsername(request.getUserCI());

        if(clientModel.isPresent()){
            ClientModel client = clientModel.get();
            if (!client.isConfirmed()){
                if (client.getEmail().equals(request.getEmail()) && client.getName().equals(request.getName())
                        && client.getLastname().equals(request.getLastname()) && client.getUserCI().equals(request.getUserCI())
                        && client.getNumber().equals(request.getNumber())
                ){
                    revokeAllClientToken(client);
                    String jwtToken = GenerateTokenAndSendEmailRegisterClient(client);
                    return AuthenticationResponse.builder()
                            .token(jwtToken)
                            .build();
                }
            }
            throw new UserAlreadyExistsException(String.format("Accountant with username %s already taken",request.getUserCI()));
        }
        String token = request.getToken();
        String accountantUsername = jwtService.getUsername(token);
        AccountantModel accountant = accountantRepository.findByUsername(accountantUsername)
                .orElseThrow(()->new UserNotFoundException(String.format("Accountant with username %s not found",accountantUsername)));
        var user = ClientModel.builder()
                .userCI(request.getUserCI())
                .email(request.getEmail())
                .name(request.getName())
                .lastname(request.getLastname())
                .number(request.getNumber())
                .password(passwordEncoder.encode(request.getUserCI()))
                .role(Role.CLIENT)
                .isEnable(false)
                .isConfirmed(false)
                .accountant_id(accountant.getId())
                .build();
        clientRepository.save(user);
        String jwtToken = GenerateTokenAndSendEmailRegisterClient(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
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
        emailSender.send(client.getEmail(),buildEmail(client.getName(),link));
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

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }
}
