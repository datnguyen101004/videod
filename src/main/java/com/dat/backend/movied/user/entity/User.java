package com.dat.backend.movied.user.entity;

import com.dat.backend.movied.auth.entity.LoginMethod;
import com.dat.backend.movied.auth.entity.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    private LoginMethod loginMethod;
}
