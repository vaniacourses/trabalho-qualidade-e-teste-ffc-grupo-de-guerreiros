package com.mycompany.a;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "login", urlPatterns = {"/login"})
public class login extends HttpServlet {

    private LoginService loginService;

    @Override
    public void init() {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        loginService = new LoginService(usuarioDAO);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessao = request.getSession();
        String erro = (String) sessao.getAttribute("erroLogin");

        if (erro != null) {
            request.setAttribute("erroLogin", erro);
            sessao.removeAttribute("erroLogin");
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/login.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String senha = request.getParameter("senha");

        try {
            Usuario usuario = loginService.autenticar(email, senha);

            HttpSession sessao = request.getSession();

            if (usuario != null) {
                sessao.setAttribute("idUsuario", usuario.getId());
                sessao.setAttribute("nomeUsuario", usuario.getNome());
                response.sendRedirect("painel");
            } else {
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