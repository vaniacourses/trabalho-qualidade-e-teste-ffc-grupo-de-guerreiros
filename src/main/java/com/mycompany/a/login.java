package com.mycompany.a;

import java.io.IOException;
import java.sql.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(name = "login", urlPatterns = {"/login"})
public class login extends HttpServlet {

    String url = "jdbc:derby://localhost:1527/trabalho";
    String usuarioBD = "eri";
    String senhaBD = "eri"; 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
            HttpSession sessao = request.getSession();
            String erro = (String) sessao.getAttribute("erroLogin");
            if(erro != null) {
                request.setAttribute("erroLogin", erro);
                sessao.removeAttribute("erroLogin");
            }
            
            // Encaminha para o JSP
            RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/login.jsp");
            dispatcher.forward(request, response);
        }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String senha = request.getParameter("senha");

        try (Connection con = DriverManager.getConnection(url, usuarioBD, senhaBD)) {
            String sql = "SELECT id, nome FROM usuario WHERE email = ? AND senha = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, email);
            pst.setString(2, senha);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");

                // Salva na sessão
                HttpSession sessao = request.getSession();
                sessao.setAttribute("idUsuario", id);
                sessao.setAttribute("nomeUsuario", nome);

                // Redireciona para menu real
                response.sendRedirect("painel");

            } else {
                // Login inválido
                HttpSession sessao = request.getSession();
                sessao.setAttribute("erroLogin", "Email ou senha incorretos.");
                response.sendRedirect("login");
            }

        } catch (SQLException e) {
            HttpSession sessao = request.getSession();
            sessao.setAttribute("erroLogin", "Erro no sistema. Tente novamente mais tarde.");
            response.sendRedirect("login");
        }
    }
}