package com.mycompany.a;

import java.sql.SQLException;

public class LoginService {

    private UsuarioDAO usuarioDAO;

    public LoginService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario autenticar(String email, String senha) throws SQLException {

        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        if (senha == null || senha.trim().isEmpty()) {
            return null;
        }

        return usuarioDAO.buscarPorEmailESenha(email, senha);
    }
}