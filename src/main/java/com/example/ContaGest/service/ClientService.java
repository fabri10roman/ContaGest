package com.example.ContaGest.service;


import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.model.ClientModel;
import com.example.ContaGest.model.InvoiceModel;
import com.example.ContaGest.repository.ClientRepository;
import com.example.ContaGest.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;

    public void saveImage (String clientCI,int month, byte[] bytes, int year) {

        InvoiceModel invoiceModel = new InvoiceModel();
        invoiceModel.setMonth(month);
        invoiceModel.setImg(bytes);
        invoiceModel.setYear(year);
        Optional<ClientModel> clientModel = clientRepository.findByUsername(clientCI);

        if (clientModel.isPresent()){
            invoiceModel.setClient(clientModel.get());
        }else{
            throw new ResourceNotFoundException(String.format("Client with CI %s and month %s not found",clientCI,month));
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
