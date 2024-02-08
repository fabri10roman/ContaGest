package com.example.ContaGest.service;


import com.example.ContaGest.dto.ResponsePayload;
import com.example.ContaGest.dto.request.ChangePersonalDataClientRequest;
import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.model.*;
import io.jsonwebtoken.ExpiredJwtException;
import com.example.ContaGest.repository.ClientRepository;
import com.example.ContaGest.repository.TokenRepository;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final TokenService tokenService;

    @Transactional
    public ResponsePayload changePersonalData(ChangePersonalDataClientRequest changePersonalDataClientRequest) throws BadRequestException {
        String username;
        try {
            username = jwtService.getUsername(changePersonalDataClientRequest.getToken());
        }catch (ExpiredJwtException e){
            TokenModel tokenModel = tokenRepository.findByToken(changePersonalDataClientRequest.getToken())
                    .orElseThrow(() -> new ResourceNotFoundException("Token not found"));
            tokenModel.setExpired(true);
            tokenModel.setRevoke(true);
            tokenRepository.save(tokenModel);
            throw new ExpiredJwtException(null,null,null);
        }catch (SignatureException e) {
            throw new SignatureException(null);
        }
        Optional<ClientModel> clientModel = clientRepository.findByUsername(username);
        if (clientModel.isPresent()){
            ClientModel client = clientModel.get();
            String newEmail = changePersonalDataClientRequest.getEmail();
            String oldEmail = client.getEmail();
            String oldName = client.getName();
            if (changePersonalDataClientRequest.getName() != null){
                client.setName(changePersonalDataClientRequest.getName());
            }
            if (changePersonalDataClientRequest.getLastName() != null){
                client.setLastname(changePersonalDataClientRequest.getLastName());
            }
            if (changePersonalDataClientRequest.getPhoneNumber() != null){
                client.setPhoneNumber(changePersonalDataClientRequest.getPhoneNumber());
            }
            if (newEmail != null && !oldEmail.equals(newEmail)){
                Optional<ClientModel> clientModelByEmail = clientRepository.findByEmail(newEmail);
                if (clientModelByEmail.isPresent()){
                    throw new ResourceNotFoundException(String.format("Email %s already taken",newEmail));
                }
                if (emailService.isEmailNotValid(newEmail)){
                    throw new ResourceNotFoundException(String.format("Email %s is not valid",newEmail));
                }
                client.setEmail(newEmail);
                client.setConfirmed(false);
                emailService.send(oldEmail, "Email changed. Your email has been changed, if you did not do this, please contact us");
                tokenService.revokeAllClientToken(client);
                tokenService.GenerateTokenAndSendEmailChangeEmailClient(client, newEmail);
            }
            clientRepository.save(client);
        }else{
            throw new ResourceNotFoundException(String.format("Client with CI %s not found",username));
        }
        return ResponsePayload.builder()
                .status(HttpStatus.OK.value())
                .title(HttpStatus.OK.getReasonPhrase())
                .message("Personal data changed successfully")
                .build();
    }

}
