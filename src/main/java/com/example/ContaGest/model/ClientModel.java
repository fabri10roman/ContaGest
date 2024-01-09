package com.example.ContaGest.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Clients")
public class ClientModel {

    @Id
    private Long ci;
    private String name;
    private String lastname;
    private String email;

    @ManyToOne
    @JoinColumn(name = "accountant_fk",nullable = false)
    private AccountantModel accountant;

    @OneToMany(mappedBy = "client")
    private List<InvoiceModel> invoices;

}
