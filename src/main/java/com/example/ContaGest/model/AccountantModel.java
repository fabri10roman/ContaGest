package com.example.ContaGest.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Accountants")
public class AccountantModel {

    @Id
    private Long ci;
    private String name;
    private String lastname;
    private String email;
    private String password;

    @OneToMany(mappedBy = "accountant")
    private List<ClientModel> clients;
}
