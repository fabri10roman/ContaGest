package com.example.ContaGest.controller;

import com.example.ContaGest.dto.PdfRequest;
import com.example.ContaGest.service.AccountantService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;


@RestController
@RequestMapping("/api/v1/accountant")
@AllArgsConstructor
public class AccountantController {

    private AccountantService accountantService;

    @GetMapping("/get-invoice")
    public ResponseEntity<ByteArrayResource> getPDF(@RequestBody PdfRequest pdfRequest) {
        byte[] data = accountantService.getPDF(pdfRequest.getClientCI(),pdfRequest.getMonth(), pdfRequest.getYear());
        ByteArrayResource resource = new ByteArrayResource(data);
        String filename = String.format("%s %s.pdf", getMonthName(pdfRequest.getMonth()), pdfRequest.getYear());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    private String getMonthName(int month) {
        String monthAndYear = "";
        switch (month) {
            case 1:
                monthAndYear = "Enero";
                break;
            case 2:
                monthAndYear = "Febrero";
                break;
            case 3:
                monthAndYear = "Marzo";
                break;
            case 4:
                monthAndYear = "Abril";
                break;
            case 5:
                monthAndYear = "Mayo";
                break;
            case 6:
                monthAndYear = "Junio";
                break;
            case 7:
                monthAndYear = "Julio";
                break;
            case 8:
                monthAndYear = "Agosto";
                break;
            case 9:
                monthAndYear = "Septiembre";
                break;
            case 10:
                monthAndYear = "Octubre";
                break;
            case 11:
                monthAndYear = "Noviembre";
                break;
            case 12:
                monthAndYear = "Diciembre";
                break;
        }
        return monthAndYear;
    }
}
