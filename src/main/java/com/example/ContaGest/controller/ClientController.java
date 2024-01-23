package com.example.ContaGest.controller;

import com.example.ContaGest.dto.ResponsePayload;
import com.example.ContaGest.dto.request.SaveImageRequest;
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
    @ResponseBody
    public ResponsePayload saveImage(SaveImageRequest saveImageRequest) throws IOException {
        return clientService.saveImage(saveImageRequest.getMonth(), saveImageRequest.getFile().getBytes(), saveImageRequest.getYear(), saveImageRequest.getToken());
    }

    @DeleteMapping("/delete-img/{imgID}")
    @ResponseBody
    public ResponsePayload deleteImage(@PathVariable Integer imgID) {
        return clientService.deleteImage(imgID);
    }

}
