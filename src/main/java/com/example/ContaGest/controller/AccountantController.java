package com.example.ContaGest.controller;

import com.example.ContaGest.dto.PdfRequest;
import com.example.ContaGest.service.AccountantService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/v1/accountant")
@AllArgsConstructor
public class AccountantController {

    private AccountantService accountantService;

    @GetMapping("/get-invoice")
    public void getPDF(@RequestBody PdfRequest pdfRequest) throws FileNotFoundException {
        accountantService.getPDF(pdfRequest.getClientCI(),pdfRequest.getMonth(),pdfRequest.getPath());
    }




}
