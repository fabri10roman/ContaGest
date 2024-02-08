package com.example.ContaGest.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ImageIdRequest {
    String month;
    String year;
    String token;
}
