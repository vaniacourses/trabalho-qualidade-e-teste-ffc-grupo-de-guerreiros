package com.mycompany.a;

import java.io.IOException;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "cadastro", urlPatterns = {"/cadastro"})
public class Cadastro extends HttpServlet {

    String url = "jdbc:derby://localhost:1527/trabalho";
    String usuarioBD = "eri";
    String senhaBD = "eri";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Encaminha para o JSP
        request.getRequestDispatcher("/WEB-INF/cadastro.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String senha = request.getParameter("senha");

        try (Connection con = DriverManager.getConnection(url, usuarioBD, senhaBD)) {
            // Inserir usuário
            String sqlUsuario = "INSERT INTO usuario (nome, email, senha) VALUES (?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, nome);
            pst.setString(2, email);
            pst.setString(3, senha);
            pst.executeUpdate();

            // Pegar ID gerado
            ResultSet rs = pst.getGeneratedKeys();
            int usuarioId = 0;
            if (rs.next()) {
                usuarioId = rs.getInt(1);
            }

            // Criar conta com número aleatório (exemplo simples)
            String numeroConta = "C" + (int)(Math.random() * 100000);
            String sqlConta = "INSERT INTO conta (numero, saldo, usuario_id) VALUES (?, 0, ?)";
            PreparedStatement pst2 = con.prepareStatement(sqlConta);
            pst2.setString(1, numeroConta);
            pst2.setInt(2, usuarioId);
            pst2.executeUpdate();

            // Redireciona para login
            response.sendRedirect("login");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Erro ao cadastrar: " + e.getMessage());
        }
    }
}