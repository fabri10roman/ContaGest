package com.example.ContaGest.service;


import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.model.InvoiceModel;
import com.example.ContaGest.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceModel getBinaryImage (Integer id){
        return invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format("Image with id %s not found",id)));
    }

}
