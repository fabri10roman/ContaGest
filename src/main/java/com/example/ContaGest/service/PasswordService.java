package com.example.ContaGest.service;

import com.example.ContaGest.dto.ChangePasswordRequest;
import com.example.ContaGest.model.AccountantModel;
import com.example.ContaGest.model.ClientModel;
import com.example.ContaGest.model.Role;
import com.example.ContaGest.repository.AccountantRepository;
import com.example.ContaGest.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.resource.beans.container.spi.BeanLifecycleStrategy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final ClientRepository clientRepository;
    private final AccountantRepository accountantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {

        String jwt = request.getToken();
        String role = jwtService.getRole(jwt);

        if (role.equals(Role.ACCOUNTANT.name())){
            changePasswordAccount(request,connectedUser);
        }else if (role.equals(Role.CLIENT.name())){
            changePasswordClient(request,connectedUser);
        }
    }

    public void changePasswordAccount(ChangePasswordRequest request, Principal connectedUser) {

        AccountantModel user = (AccountantModel) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("The current password does not match");
        }
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new BadCredentialsException("Password are not the same");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountantRepository.save(user);

    }

    public void changePasswordClient(ChangePasswordRequest request, Principal connectedUser) {

        ClientModel user = (ClientModel) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("The current password does not match");
        }
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new BadCredentialsException("Password are not the same");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        clientRepository.save(user);

    }
}
