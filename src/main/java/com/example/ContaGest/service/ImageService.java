package com.example.ContaGest.service;


import com.example.ContaGest.dto.ResponsePayload;
import com.example.ContaGest.dto.request.ImageIdRequest;
import com.example.ContaGest.dto.response.SaveImageResponse;
import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.model.ClientModel;
import com.example.ContaGest.model.InvoiceModel;
import com.example.ContaGest.model.TokenModel;
import com.example.ContaGest.repository.ClientRepository;
import com.example.ContaGest.repository.InvoiceRepository;
import com.example.ContaGest.repository.TokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final InvoiceRepository invoiceRepository;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final ClientRepository clientRepository;
    public InvoiceModel getBinaryImage (Integer id){
        return invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format("Image with id %s not found",id)));
    }

    public ResponsePayload saveImage (int month, byte[] bytes, int year, String token) {
        String username;
        try {
            username = jwtService.getUsername(token);
        }catch (ExpiredJwtException e){
            TokenModel tokenModel = tokenRepository.findByToken(token)
                    .orElseThrow(() -> new ResourceNotFoundException("Token not found"));
            tokenModel.setExpired(true);
            tokenModel.setRevoke(true);
            tokenRepository.save(tokenModel);
            throw new ExpiredJwtException(null,null,null);
        }catch (SignatureException e) {
            throw new SignatureException(null);
        }
        InvoiceModel invoiceModel = new InvoiceModel();
        invoiceModel.setMonth(month);
        invoiceModel.setImg(bytes);
        invoiceModel.setYear(year);
        Optional<ClientModel> clientModel = clientRepository.findByUsername(username);
        if (clientModel.isPresent()){
            invoiceModel.setClient(clientModel.get());
        }else{
            throw new ResourceNotFoundException(String.format("Client with CI %s not found",username));
        }
        invoiceRepository.save(invoiceModel);
        Integer id = invoiceModel.getId();
        List<Object> data;
        SaveImageResponse saveImageResponse = SaveImageResponse.builder()
                .id(id)
                .month(month)
                .year(year)
                .build();
        data = List.of(saveImageResponse);
        return ResponsePayload.builder()
                .status(HttpStatus.OK.value())
                .title(HttpStatus.OK.getReasonPhrase())
                .message("Image saved successfully")
                .data(data)
                .build();
    }

    public ResponsePayload deleteImage (Integer imageID){

        Optional<InvoiceModel> invoiceModel = invoiceRepository.findById(imageID);

        if (invoiceModel.isPresent()){
            invoiceRepository.delete(invoiceModel.get());
        }else{
            throw new ResourceNotFoundException(String.format("Image with id %s not found",imageID));
        }
        return ResponsePayload.builder()
                .status(HttpStatus.OK.value())
                .title(HttpStatus.OK.getReasonPhrase())
                .message("Image deleted successfully")
                .build();
    }

    public ResponsePayload getImagesId(ImageIdRequest imageIdRequest) {
        String username;
        TokenModel tokenModel = tokenRepository.findByToken(imageIdRequest.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        try {
            username = jwtService.getUsername(imageIdRequest.getToken());
        }catch (ExpiredJwtException e){
            tokenModel.setExpired(true);
            tokenModel.setRevoke(true);
            tokenRepository.save(tokenModel);
            throw new ExpiredJwtException(null,null,null);
        }catch (SignatureException e) {
            throw new SignatureException(null);
        }
        Optional<ClientModel> clientModel = clientRepository.findByUsername(username);
        if (clientModel.isPresent()){
            var imagesId = invoiceRepository.findImagesId(imageIdRequest.getMonth(),imageIdRequest.getYear(),clientModel.get().getId());
            if (imagesId.isEmpty()){
                throw new ResourceNotFoundException(String.format("Images with month %s and year %s not found for the client with CI %s"
                        ,imageIdRequest.getMonth(),imageIdRequest.getYear(),username));
            }
            return ResponsePayload.builder()
                    .status(HttpStatus.OK.value())
                    .title(HttpStatus.OK.getReasonPhrase())
                    .message("Images id found successfully")
                    .data(Collections.singletonList(imagesId))
                    .build();
        }else{
            throw new ResourceNotFoundException(String.format("Client with CI %s not found",username));
        }
    }
}
