package com.example.ContaGest.controller;

import com.example.ContaGest.dto.*;
import com.example.ContaGest.dto.request.LoginRequest;
import com.example.ContaGest.dto.request.RegisterAccountantRequest;
import com.example.ContaGest.dto.request.RegisterClientRequest;
import com.example.ContaGest.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    @PostMapping("/login")
    @ResponseBody
    public ResponsePayload login(@RequestBody LoginRequest request){
        return authenticationService.login(request);
    }

    @PostMapping("/register-accountant")
    @ResponseBody
    public ResponsePayload registerAccountant(@RequestBody RegisterAccountantRequest request) throws BadRequestException {
        return authenticationService.registerAccountant(request);
    }

    @GetMapping("/confirm-accountant")
    @ResponseBody
    public ResponsePayload confirmAccountant(@RequestParam("token") String token){
        return authenticationService.confirmTokenRegistration(token);
    }

    @GetMapping("/confirm-client")
    @ResponseBody
    public ResponsePayload confirmClient(@RequestParam("token") String token){
        return authenticationService.confirmTokenRegistration(token);
    }

    @PostMapping("/register-client")
    @ResponseBody
    public ResponsePayload registerClient(@RequestBody RegisterClientRequest request,
                                          @RequestHeader("Authorization") String barerToken) throws BadRequestException {
        String token = barerToken.substring(7);
        request.setToken(token);
        return authenticationService.registerClient(request);
    }

    @GetMapping("/confirm-change-email")
    @ResponseBody
    public ResponsePayload confirmChangeEmail(@RequestParam("token") String token){
        return authenticationService.confirmChangeEmail(token);
    }
}
