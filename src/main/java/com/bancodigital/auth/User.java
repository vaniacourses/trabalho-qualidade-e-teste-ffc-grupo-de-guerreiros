package com.bancodigital.auth;

public record User(Long id, String name, String email, String passwordHash) {
}
