package com.mycompany.a;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    private String url = "jdbc:derby://localhost:1527/trabalho";
    private String usuarioBD = "eri";
    private String senhaBD = "eri";

    public Usuario buscarPorEmailESenha(String email, String senha) throws SQLException {
        String sql = "SELECT id, nome FROM usuario WHERE email = ? AND senha = ?";

        try (Connection con = DriverManager.getConnection(url, usuarioBD, senhaBD);
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, email);
            pst.setString(2, senha);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        rs.getInt("id"),
                        rs.getString("nome")
                    );
                }
            }
        }

        return null;
    }
}