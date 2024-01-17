package com.example.ContaGest.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SaveImage {

    private MultipartFile file;
    private int month;
    private int year;
    private String token;
}
