package com.example.ContaGest.service;


import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.model.ClientModel;
import com.example.ContaGest.model.InvoiceModel;
import com.example.ContaGest.repository.ClientRepository;
import com.example.ContaGest.repository.InvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;

    public void saveImage (Long clientCI,int month,String path) throws IOException {

        File file = new File(path);
        byte[] fileContent = Files.readAllBytes(file.toPath());

        InvoiceModel invoiceModel = new InvoiceModel();
        invoiceModel.setMonth(month);
        invoiceModel.setImg(fileContent);
        Optional<ClientModel> clientModel = clientRepository.findById(clientCI);

        if (clientModel.isPresent()){
            invoiceModel.setClient(clientModel.get());
        }else{
            throw new ResourceNotFoundException("No se encontró ningún cliente con CI: " + clientCI);
        }

        invoiceRepository.save(invoiceModel);
    }



}
