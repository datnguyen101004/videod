package com.dat.backend.movied.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "user_login")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    private LoginMethod loginMethod;
}
