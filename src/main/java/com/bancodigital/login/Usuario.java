package com.bancodigital.login;

public record Usuario(Long id, String nome, String email, String senhaHash) {
}
