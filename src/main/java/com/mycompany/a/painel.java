package com.mycompany.a;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "painel", urlPatterns = {"/painel"})
public class painel extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession sessao = request.getSession(false);

        // Verificar se o usuário está logado
        if (sessao == null || sessao.getAttribute("idUsuario") == null) {
            response.sendRedirect("login");
            return;
        }

        String acao = request.getParameter("acao");
        
        // Processar logout
        if ("logout".equals(acao)) {
            sessao.invalidate();
            response.sendRedirect("menu");
            return;
        }

        // Obter dados do usuário da sessão
        String nome = (String) sessao.getAttribute("nomeUsuario");
        
        // Passar dados para o JSP
        request.setAttribute("nomeUsuario", nome);
        
        // Redirecionar para o JSP - MESMO PADRÃO DO LOGIN
        RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/painel.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}