package com.example.ContaGest.service;


import com.example.ContaGest.exception.ResourceNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import com.example.ContaGest.model.ClientModel;
import com.example.ContaGest.model.InvoiceModel;
import com.example.ContaGest.model.TokenModel;
import com.example.ContaGest.repository.ClientRepository;
import com.example.ContaGest.repository.InvoiceRepository;
import com.example.ContaGest.repository.TokenRepository;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    public void saveImage (int month, byte[] bytes, int year, String token) {
        String username;
        try {
            username = jwtService.getUsername(token);
        }catch (ExpiredJwtException e){
            TokenModel tokenModel = tokenRepository.findByToken(token)
                    .orElseThrow(() -> new ResourceNotFoundException(String.format("Token %s not found",token)));
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
            throw new ResourceNotFoundException(String.format("Client with CI %s and month %s not found",username,month));
        }
        invoiceRepository.save(invoiceModel);
    }

    public void deleteImage (Integer imageID){

        Optional<InvoiceModel> invoiceModel = invoiceRepository.findById(imageID);

        if (invoiceModel.isPresent()){
            invoiceRepository.delete(invoiceModel.get());
        }else{
            throw new ResourceNotFoundException(String.format("Image with id %s not found",imageID));
        }

    }


}
