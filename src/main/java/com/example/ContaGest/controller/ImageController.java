package com.example.ContaGest.controller;

import com.example.ContaGest.dto.ImageRequest;
import com.example.ContaGest.service.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/month")
    public List<Integer> getAllIdImagePerMonth(@RequestBody ImageRequest imageRequest){
        return imageService.findIdByClientCiAndMonth(imageRequest.getClientCI(),imageRequest.getMonth(),imageRequest.getYear());
    }
}
