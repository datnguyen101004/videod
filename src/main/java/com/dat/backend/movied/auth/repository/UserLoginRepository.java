package com.dat.backend.movied.auth.repository;

import com.dat.backend.movied.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserLoginRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
