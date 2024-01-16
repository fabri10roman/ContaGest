package com.example.ContaGest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "Tokens")
public class TokenModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private String token;
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;
    private boolean isExpired;
    private boolean isRevoke;
    private boolean isRegistration;
    private boolean isForgotPassword;
    private Integer accountant_id;
    private Integer client_id;


    @ManyToOne
    @JoinColumn(name = "accountant_id", referencedColumnName = "id", insertable = false, updatable = false)
    private AccountantModel accountant;

    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "id", insertable = false, updatable = false)
    private ClientModel client;
}
