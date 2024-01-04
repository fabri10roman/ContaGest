package com.example.ContaGest.controller;

import com.example.ContaGest.dto.SaveImage;
import com.example.ContaGest.service.ClientService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/client")
@AllArgsConstructor
public class ClientController {

    private ClientService clientService;

    @PostMapping("/save-img")
    public void saveImage(@RequestBody SaveImage saveImage) throws IOException {

        //String path = "/home/ubuntu/Descargas/WhatsApp Image 2024-01-01 at 8.05.44 PM.jpeg";

        clientService.saveImage(saveImage.getClientCI(),saveImage.getMonth(),saveImage.getPath());
    }

}
