package com.example.ContaGest.dto;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SaveImage {

    private Long clientCI;
    private int month;
    private String path;
}
