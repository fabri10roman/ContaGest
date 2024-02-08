package com.example.ContaGest.service;

import com.example.ContaGest.dto.ResponsePayload;
import com.example.ContaGest.dto.request.ChangePersonalDataAccountantRequest;
import com.example.ContaGest.dto.response.ClientResponse;
import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.model.*;
import com.example.ContaGest.repository.AccountantRepository;
import com.example.ContaGest.repository.InvoiceRepository;
import com.example.ContaGest.repository.TokenRepository;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;


import java.io.ByteArrayOutputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountantService {

    private final InvoiceRepository invoiceRepository;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final AccountantRepository accountantRepository;
    private final AuthenticationService authenticationService;
    private final EmailService emailService;

    public byte[] getPDF(String clientCI, int month, int year) {

        List<InvoiceModel> invoices = invoiceRepository.findByClientIdAndMonthAndYear(clientCI, month, year);
        List<byte[]> imgs = invoices.stream().map(InvoiceModel::getImg).toList();

        if (imgs.isEmpty()) {
            throw new ResourceNotFoundException(String.format("Invoices of the client with CI %s in month %s and year %s not found", clientCI, month, year));
        }

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(byteArrayOutputStream);

            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.addNewPage();

            Document document = new Document(pdfDocument);

            for (byte[] img : imgs) {
                ImageData imageData = ImageDataFactory.create(img);
                Image image = new Image(imageData);
                float width = pdfDocument.getDefaultPageSize().getWidth() - document.getLeftMargin() - document.getRightMargin();
                float height = pdfDocument.getDefaultPageSize().getHeight() - document.getTopMargin() - document.getBottomMargin();
                image.scaleToFit(width, height);
                Div div = new Div();
                div.setVerticalAlignment(VerticalAlignment.MIDDLE);
                div.setHorizontalAlignment(HorizontalAlignment.CENTER);
                div.add(image);
                div.setMarginTop(20);
                document.add(div);
            }
            document.close();
            return byteArrayOutputStream.toByteArray();
        }catch (Exception e){
            throw new IllegalStateException("Error to generate PDF");
        }
    }
    public String getMonthName(int month) {
        return switch (month) {
            case 1 -> "Enero";
            case 2 -> "Febrero";
            case 3 -> "Marzo";
            case 4 -> "Abril";
            case 5 -> "Mayo";
            case 6 -> "Junio";
            case 7 -> "Julio";
            case 8 -> "Agosto";
            case 9 -> "Septiembre";
            case 10 -> "Octubre";
            case 11 -> "Noviembre";
            case 12 -> "Diciembre";
            default -> "";
        };
    }


    public ResponsePayload changePersonalData (ChangePersonalDataAccountantRequest changePersonalDataAccountantRequest) throws BadRequestException {
        String username;
        try {
            username = jwtService.getUsername(changePersonalDataAccountantRequest.getToken());
        }catch (ExpiredJwtException e){
            TokenModel tokenModel = tokenRepository.findByToken(changePersonalDataAccountantRequest.getToken())
                    .orElseThrow(() -> new ResourceNotFoundException("Token not found"));
            tokenModel.setExpired(true);
            tokenModel.setRevoke(true);
            tokenRepository.save(tokenModel);
            throw new ExpiredJwtException(null,null,null);
        }catch (SignatureException e) {
            throw new SignatureException(null);
        }
        Optional<AccountantModel> accountantModel = accountantRepository.findByUsername(username);
        if (accountantModel.isPresent()){
            AccountantModel accountant = accountantModel.get();
            String newEmail = changePersonalDataAccountantRequest.getEmail();
            String oldEmail = accountant.getEmail();
            String oldName = accountant.getName();
            if (changePersonalDataAccountantRequest.getName() != null){
                accountant.setName(changePersonalDataAccountantRequest.getName());
            }
            if (changePersonalDataAccountantRequest.getLastName() != null){
                accountant.setLastname(changePersonalDataAccountantRequest.getLastName());
            }
            if (changePersonalDataAccountantRequest.getPhoneNumber() != null){
                accountant.setPhoneNumber(changePersonalDataAccountantRequest.getPhoneNumber());
            }
            if (newEmail != null && !oldEmail.equals(newEmail)){
                Optional<AccountantModel> accountantModelByEmail = accountantRepository.findByEmail(newEmail);
                if (accountantModelByEmail.isPresent()){
                    throw new ResourceNotFoundException(String.format("Email %s already taken",newEmail));
                }
                if (authenticationService.isEmailNotValid(newEmail)){
                    throw new ResourceNotFoundException(String.format("Email %s is not valid",newEmail));
                }
                accountant.setEmail(newEmail);
                accountant.setConfirmed(false);
                emailService.send(oldEmail, "Email changed. Your email has been changed, if you did not do this, please contact us");
                authenticationService.revokeAllAccountantToken(accountant);
                authenticationService.GenerateTokenAndSendEmailChangeEmailAccountant(accountant, newEmail);
            }
            accountantRepository.save(accountant);
        }else{
            throw new ResourceNotFoundException(String.format("Accountant with CI %s not found",username));
        }
        return ResponsePayload.builder()
                .message("Personal data changed successfully")
                .build();
    }


    public ResponsePayload getClient(String bearerToken) {
        String token = bearerToken.substring(7);
        String username;
        TokenModel tokenModel = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        try {
            username = jwtService.getUsername(token);
        }catch (ExpiredJwtException e){
            tokenModel.setExpired(true);
            tokenModel.setRevoke(true);
            tokenRepository.save(tokenModel);
            throw new ExpiredJwtException(null,null,null);
        }catch (SignatureException e) {
            throw new SignatureException(null);
        }
        if (tokenModel.isRevoke() || tokenModel.isExpired()){
            throw new ResourceNotFoundException("Token expired");
        }
        Optional<AccountantModel> accountantModel = accountantRepository.findByUsername(username);
        if (accountantModel.isPresent()){
            List<ClientResponse> data = getClientResponses(accountantModel.get());
            return ResponsePayload.builder()
                    .message("Clients found successfully")
                    .data(Collections.singletonList(data))
                    .build();
        }else{
            throw new ResourceNotFoundException(String.format("Accountant with CI %s not found",username));
        }
    }

    private static List<ClientResponse> getClientResponses(AccountantModel accountant) {
        List<ClientModel> clients = accountant.getClients();
        List<ClientResponse> data = new ArrayList<>();
        clients.forEach(client -> {
            if (client.isEnable() && client.isConfirmed()) {
                ClientResponse clientResponse = new ClientResponse();
                clientResponse.setId(client.getId());
                clientResponse.setCi(client.getCi());
                clientResponse.setName(client.getName());
                clientResponse.setLastname(client.getLastname());
                clientResponse.setEmail(client.getEmail());
                clientResponse.setPhoneNumber(client.getPhoneNumber());
                data.add(clientResponse);
            }
        });
        return data;
    }
}
