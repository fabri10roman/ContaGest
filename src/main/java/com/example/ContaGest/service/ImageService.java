package com.example.ContaGest.service;

import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.model.InvoiceModel;
import com.example.ContaGest.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceModel getBinaryImage (Integer id){

        return invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format("Image with id %s not found",id)));
    }

    public List<Integer> findIdByClientCI (String clientCI, int year){

        List<Integer> imgID = invoiceRepository.findIdByClientCI(clientCI,year);

        if (imgID.isEmpty()) throw new ResourceNotFoundException(String.format("ID with client CI %s not found",clientCI));

        return imgID;
    }

    public List<Integer> findIdByClientCiAndMonth(String clientCI, int month, int year){

        List<Integer> imgID = invoiceRepository.findIdByClientCiAndMonthAndYear(clientCI,month,year);

        if (imgID.isEmpty()) throw new ResourceNotFoundException(String.format("ID with client CI %s and month %s not found",clientCI,month));

        return imgID;
        
    }
}
