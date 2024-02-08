package com.example.ContaGest.controller;

import com.example.ContaGest.dto.ResponsePayload;
import com.example.ContaGest.dto.request.ChangePersonalDataAccountantRequest;
import com.example.ContaGest.dto.request.PdfRequest;
import com.example.ContaGest.service.AccountantService;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
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
        String filename = String.format("%s %s.pdf", accountantService.getMonthName(pdfRequest.getMonth()), pdfRequest.getYear());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
    @PostMapping("/change-personal-data")
    @ResponseBody
    public ResponsePayload changePersonalData(@RequestBody ChangePersonalDataAccountantRequest changePersonalDataAccountantRequest) throws BadRequestException {
        return accountantService.changePersonalData(changePersonalDataAccountantRequest);
    }
    @GetMapping("/get-clients")
    @ResponseBody
    public ResponsePayload getClient(@RequestHeader("Authorization") String bearerToken) {
        return accountantService.getClient(bearerToken);
    }
}
