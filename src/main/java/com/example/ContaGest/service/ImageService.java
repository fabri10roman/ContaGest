package com.example.ContaGest.service;

import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.model.InvoiceModel;
import com.example.ContaGest.repository.InvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ImageService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceModel getBinaryImage (Long id){

        return invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format("Image with id %s not found",id)));
    }

    public List<Long> findIdByClientCI (Long clientCI){

        List<Long> imgID = invoiceRepository.findIdByClientCI(clientCI);

        if (imgID.isEmpty()) throw new ResourceNotFoundException(String.format("ID with clientCI %s not found",clientCI));

        return imgID;
    }

    public List<Long> findIdByClientCiAndMonth(Long clientCI, int month){

        List<Long> imgID = invoiceRepository.findIdByClientCiAndMonth(clientCI,month);

        if (imgID.isEmpty()) throw new ResourceNotFoundException(String.format("ID with clientCI %s and month %s not found",clientCI,month));

        return imgID;
        
    }
}
