package com.example.ContaGest.controller;

import com.example.ContaGest.dto.ResponsePayload;
import com.example.ContaGest.dto.request.ImageIdRequest;
import com.example.ContaGest.dto.request.SaveImageRequest;
import com.example.ContaGest.service.ClientService;
import com.example.ContaGest.service.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/img")
@AllArgsConstructor
public class ImageController {

    private ImageService imageService;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getBinaryImage(@PathVariable Integer id){
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageService.getBinaryImage(id).getImg());
    }
    @PostMapping()
    @ResponseBody
    public ResponsePayload saveImage(SaveImageRequest saveImageRequest,
                                     @RequestHeader("Authorization") String barerToken) throws IOException {
        String token = barerToken.substring(7);
        saveImageRequest.setToken(token);
        return imageService.saveImage(saveImageRequest.getMonth(), saveImageRequest.getFile().getBytes(), saveImageRequest.getYear(), saveImageRequest.getToken());
    }

    @DeleteMapping("/{imgID}")
    @ResponseBody
    public ResponsePayload deleteImage(@PathVariable Integer imgID) {
        return imageService.deleteImage(imgID);
    }
    @GetMapping()
    public ResponsePayload getImagesId(@RequestBody ImageIdRequest imageIdRequest,
                                       @RequestHeader("Authorization") String barerToken){
        String token = barerToken.substring(7);
        imageIdRequest.setToken(token);
        return imageService.getImagesId(imageIdRequest);
    }
}
