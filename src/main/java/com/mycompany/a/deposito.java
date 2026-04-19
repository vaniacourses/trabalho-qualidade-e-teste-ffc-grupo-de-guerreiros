package com.mycompany.a;

import java.io.IOException;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(name = "deposito", urlPatterns = {"/deposito"})
public class deposito extends HttpServlet {

    private final String URL  = "jdbc:derby://localhost:1527/trabalho";
    private final String USER = "eri";
    private final String PASS = "eri";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession sessao = req.getSession(false);
        if (sessao == null || sessao.getAttribute("idUsuario") == null) {
            resp.sendRedirect("login");
            return;
        }

        int idUsuario = (int) sessao.getAttribute("idUsuario");
        double saldo = 0;
        String conta = "";

        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT id, numero, saldo FROM conta WHERE usuario_id = ?");
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                conta = rs.getString("numero");
                saldo = rs.getDouble("saldo");
                sessao.setAttribute("idConta", rs.getInt("id"));
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }

        // Passa dados para o JSP
        req.setAttribute("conta", conta);
        req.setAttribute("saldo", saldo);
        
        // Encaminha para o JSP
        req.getRequestDispatcher("/WEB-INF/deposito.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession sessao = req.getSession(false);
        if (sessao == null || sessao.getAttribute("idUsuario") == null) {
            resp.sendRedirect("login");
            return;
        }

        double valor = 0;
        try { 
            valor = Double.parseDouble(req.getParameter("valor")); 
        } catch (NumberFormatException ignored){}

        if (valor <= 0) {
            sessao.setAttribute("msgDeposito", "Valor inválido.");
            resp.sendRedirect("deposito");
            return;
        }

        int idConta = (int) sessao.getAttribute("idConta");

        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            con.setAutoCommit(false);

            // 1. Atualiza saldo
            PreparedStatement ps1 = con.prepareStatement(
                "UPDATE conta SET saldo = saldo + ? WHERE id = ?"
            );
            ps1.setDouble(1, valor);
            ps1.setInt(2, idConta);
            ps1.executeUpdate();

            // 2. Registra transação
            PreparedStatement ps2 = con.prepareStatement(
                "INSERT INTO transacao (conta_destino, tipo, valor) VALUES (?, 'deposito', ?)"
            );
            ps2.setInt(1, idConta);
            ps2.setDouble(2, valor);
            ps2.executeUpdate();

            con.commit();
            sessao.setAttribute("msgDeposito", "Depósito realizado com sucesso!");

        } catch (SQLException e) {
            e.printStackTrace();
            sessao.setAttribute("msgDeposito", "Erro ao depositar: " + e.getMessage());
        }

        resp.sendRedirect("deposito");
    }
}