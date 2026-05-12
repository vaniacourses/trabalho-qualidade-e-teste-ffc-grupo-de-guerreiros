package com.bancodigital.auth;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long save(String name, String email, String passwordHash);
}
