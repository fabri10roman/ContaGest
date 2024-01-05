package com.example.ContaGest.controller;

import com.example.ContaGest.service.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/img")
@AllArgsConstructor
public class ImageController {

    private ImageService imageService;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getBinaryImage(@PathVariable Long id){
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageService.getBinaryImage(id).getImg());
    }

}
