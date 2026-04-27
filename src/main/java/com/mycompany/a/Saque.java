package com.mycompany.a;

import java.io.IOException;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(name = "saque", urlPatterns = {"/saque"})
public class Saque extends HttpServlet {

    private static final double LIMITE_SAQUE_DIARIO = 10000.0;

    public String validaSaque(double valorConta, double valorSacado) {
        if (valorSacado <= 0) {
            return "Valor Inválido.";
        }
        if (valorSacado > valorConta) {
            return "Saldo insuficiente.";
        }
        if (valorSacado > LIMITE_SAQUE_DIARIO) {
            return "Limite máximo por saque excedido.";
        }

        return "Ok";
    }

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

        try (Connection con = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())) {
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
        
        // Redireciona para o JSP
        req.getRequestDispatcher("/WEB-INF/saque.jsp").forward(req, resp);
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
        } catch (NumberFormatException ignored) {}

        if (valor <= 0) {
            sessao.setAttribute("msgSaque", "Valor inválido.");
            resp.sendRedirect("saque");
            return;
        }

        if (valor > LIMITE_SAQUE_DIARIO) {
            sessao.setAttribute("msgSaque", "Limite máximo por saque excedido.");
            resp.sendRedirect("saque");
            return;
        }

        int idConta = (int) sessao.getAttribute("idConta");

        try (Connection con = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())) {
            con.setAutoCommit(false);

            // Verifica saldo
            PreparedStatement check = con.prepareStatement("SELECT saldo FROM conta WHERE id = ?");
            check.setInt(1, idConta);
            ResultSet rs = check.executeQuery();
            double saldoAtual = 0;
            if (rs.next()) saldoAtual = rs.getDouble("saldo");

            if (saldoAtual <= 0 || saldoAtual < valor) {
                sessao.setAttribute("msgSaque", "Saldo insuficiente.");
                resp.sendRedirect("saque");
                return;
            }

            // 1. Atualiza saldo
            PreparedStatement ps1 = con.prepareStatement(
                "UPDATE conta SET saldo = saldo - ? WHERE id = ?"
            );
            ps1.setDouble(1, valor);
            ps1.setInt(2, idConta);
            ps1.executeUpdate();

            // 2. Registra transação
            PreparedStatement ps2 = con.prepareStatement(
                "INSERT INTO transacao (conta_origem, tipo, valor) VALUES (?, 'saque', ?)"
            );
            ps2.setInt(1, idConta);
            ps2.setDouble(2, valor);
            ps2.executeUpdate();

            con.commit();
            sessao.setAttribute("msgSaque", "Saque realizado com sucesso!");

        } catch (SQLException e) {
            e.printStackTrace();
            sessao.setAttribute("msgSaque", "Erro ao sacar: " + e.getMessage());
        }

        resp.sendRedirect("saque");
    }
}