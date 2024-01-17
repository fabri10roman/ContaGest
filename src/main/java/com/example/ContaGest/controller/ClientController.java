package com.example.ContaGest.controller;

import com.example.ContaGest.dto.SaveImage;
import com.example.ContaGest.service.ClientService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/client")
@AllArgsConstructor
public class ClientController {

    private ClientService clientService;

    @PostMapping("/save-img")
    public void saveImage(SaveImage saveImage) throws IOException {
        clientService.saveImage(saveImage.getMonth(), saveImage.getFile().getBytes(), saveImage.getYear(),saveImage.getToken());
    }

    @DeleteMapping("/delete-img/{imgID}")
    public void deleteImage(@PathVariable Integer imgID) {
        clientService.deleteImage(imgID);
    }

}
