package com.mycompany.a;

import java.io.IOException;
import java.sql.*;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "cadastro", urlPatterns = {"/cadastro"})
public class Cadastro extends HttpServlet {

    private static final Pattern EMAIL_REGEX =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int SENHA_MIN = 8;

    public String validarCadastro(String nome, String email, String senha) {
        if (nome == null || nome.isBlank()) return "Nome inválido.";
        if (email == null || email.isBlank()) return "Email inválido.";
        if (!EMAIL_REGEX.matcher(email).matches()) return "Formato de email inválido.";
        if (senha == null || senha.isBlank()) return "Senha inválida.";
        if (senha.length() < SENHA_MIN) return "Senha deve ter no mínimo " + SENHA_MIN + " caracteres.";
        return "OK";
    }

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

        String erro = validarCadastro(nome, email, senha);
        if (!"OK".equals(erro)) {
            request.setAttribute("erroCadastro", erro);
            request.getRequestDispatcher("/WEB-INF/cadastro.jsp").forward(request, response);
            return;
        }

        try (Connection con = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())) {
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