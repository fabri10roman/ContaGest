package com.example.ContaGest.dto.request;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PdfRequest {

    private String clientCI;
    private int month;
    private int year;

}
