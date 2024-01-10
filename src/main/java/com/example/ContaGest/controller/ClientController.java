package com.example.ContaGest.controller;

import com.example.ContaGest.dto.SaveImage;
import com.example.ContaGest.service.ClientService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/client")
@AllArgsConstructor
public class ClientController {

    private ClientService clientService;

    @PostMapping("/save-img")
    public void saveImage(SaveImage saveImage) throws IOException {
        clientService.saveImage(saveImage.getClientCI(),saveImage.getMonth(),saveImage.getFile().getBytes());
    }

    @DeleteMapping("/delete-img/{imgID}")
    public void deleteImage (@PathVariable Long imgID){
        clientService.deleteImage(imgID);
    }

}
