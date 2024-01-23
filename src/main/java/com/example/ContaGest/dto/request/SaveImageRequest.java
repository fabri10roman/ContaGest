package com.example.ContaGest.dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SaveImageRequest {

    private MultipartFile file;
    private int month;
    private int year;
    private String token;
}
