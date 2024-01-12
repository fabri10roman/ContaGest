package com.example.ContaGest.config;

import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.exception.UserNotFoundException;
import com.example.ContaGest.model.AccountantModel;
import com.example.ContaGest.model.ClientModel;
import com.example.ContaGest.repository.AccountantRepository;
import com.example.ContaGest.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GlobalUserDetailService implements UserDetailsService {

    private final ClientRepository clientRepository;
    private final AccountantRepository accountantRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AccountantModel> accountantModel = accountantRepository.findByUsername(username);
        if (accountantModel.isPresent()){
            return accountantModel.get();
        }
        Optional<ClientModel> clientModel = clientRepository.findByUsername(username);
        return clientModel.orElseThrow(()-> new UserNotFoundException("User not found with username: " + username));
    }
}
