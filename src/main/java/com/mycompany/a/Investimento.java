package com.mycompany.a;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(name = "investimento", urlPatterns = {"/investimento"})
public class Investimento extends HttpServlet {

    private static final double TAXA_JUROS_POR_MINUTO = 1.01;

    /* ---------- UTIL Lazy‑Update ---------- */
    private BigDecimal lazyUpdate(Connection con, int userId) throws SQLException {

        PreparedStatement psSel = con.prepareStatement(
            "SELECT id, valor, ultima_att FROM investimento WHERE usuario_id = ?");
        psSel.setInt(1, userId);
        ResultSet rs = psSel.executeQuery();

        Timestamp agoraTs = new Timestamp(System.currentTimeMillis());

        if (!rs.next()) {
            // cria registro zerado
            PreparedStatement psIns = con.prepareStatement(
                "INSERT INTO investimento (usuario_id, valor, ultima_att) VALUES (?, 0, ?)");
            psIns.setInt(1, userId);
            psIns.setTimestamp(2, agoraTs);
            psIns.executeUpdate();
            return BigDecimal.ZERO;
        }

        int investId = rs.getInt("id");
        BigDecimal valor = rs.getBigDecimal("valor");
        Timestamp ultima = rs.getTimestamp("ultima_att");

        long minutos = Duration.between(ultima.toInstant(), Instant.now()).toMinutes();
        if (minutos > 0) {
            double fator = Math.pow(TAXA_JUROS_POR_MINUTO, minutos);          // 1 % ao minuto
            valor = valor.multiply(BigDecimal.valueOf(fator))
                         .setScale(2, RoundingMode.HALF_UP);

            PreparedStatement psUp = con.prepareStatement(
                "UPDATE investimento SET valor = ?, ultima_att = ? WHERE id = ?");
            psUp.setBigDecimal(1, valor);
            psUp.setTimestamp(2, agoraTs);
            psUp.setInt(3, investId);
            psUp.executeUpdate();
        }
        return valor;
    }

    /* ---------- GET ---------- */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession ses = req.getSession(false);
        if (ses == null || ses.getAttribute("idUsuario") == null) {
            resp.sendRedirect("login");
            return;
        }
        int userId = (int) ses.getAttribute("idUsuario");

        BigDecimal valorAtual = BigDecimal.ZERO;
        try (Connection con = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())) {
            valorAtual = lazyUpdate(con, userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Passa o valor atual para o JSP
        req.setAttribute("valorAtual", valorAtual);
        req.getRequestDispatcher("/WEB-INF/investimento.jsp").forward(req, resp);
    }

    /* ---------- POST ---------- */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession ses = req.getSession(false);
        if (ses == null || ses.getAttribute("idUsuario") == null) {
            resp.sendRedirect("login");
            return;
        }
        int userId = (int) ses.getAttribute("idUsuario");
        String op   = req.getParameter("op");          // investir ou retirar
        BigDecimal valor;
        try { valor = new BigDecimal(req.getParameter("valor")).setScale(2); }
        catch (Exception e){ ses.setAttribute("erroInv","Valor inválido."); resp.sendRedirect("investimento"); return; }

        try (Connection con = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())) {
            con.setAutoCommit(false);

            /* Lazy‑update + busca saldo conta */
            BigDecimal valorInvest = lazyUpdate(con, userId);

            PreparedStatement psConta = con.prepareStatement(
                "SELECT id, saldo FROM conta WHERE usuario_id = ?");
            psConta.setInt(1, userId);
            ResultSet rsC = psConta.executeQuery();
            if (!rsC.next()) throw new SQLException("Conta não encontrada");
            int idConta = rsC.getInt("id");
            BigDecimal saldoConta = rsC.getBigDecimal("saldo");

            if ("investir".equals(op)) {
                if (saldoConta.compareTo(valor) < 0) {
                    ses.setAttribute("erroInv", "Saldo insuficiente na conta.");
                    resp.sendRedirect("investimento"); return;
                }

                // debita conta
                PreparedStatement deb = con.prepareStatement(
                    "UPDATE conta SET saldo = saldo - ? WHERE id = ?");
                deb.setBigDecimal(1, valor); deb.setInt(2, idConta); deb.executeUpdate();

                // credita investimento
                valorInvest = valorInvest.add(valor);
                PreparedStatement upInv = con.prepareStatement(
                    "UPDATE investimento SET valor = ?, ultima_att = CURRENT_TIMESTAMP WHERE usuario_id = ?");
                upInv.setBigDecimal(1, valorInvest); upInv.setInt(2, userId); upInv.executeUpdate();

                // transação
                PreparedStatement pt = con.prepareStatement(
                    "INSERT INTO transacao (conta_origem, tipo, valor) VALUES (?, 'investimento', ?)");
                pt.setInt(1, idConta); pt.setBigDecimal(2, valor); pt.executeUpdate();

                ses.setAttribute("msgInv", "Investimento realizado com sucesso!");

            } else if ("retirar".equals(op)) {
                if (valorInvest.compareTo(valor) < 0) {
                    ses.setAttribute("erroInv", "Valor maior que o investido.");
                    resp.sendRedirect("investimento"); return;
                }

                // credita conta
                PreparedStatement cred = con.prepareStatement(
                    "UPDATE conta SET saldo = saldo + ? WHERE id = ?");
                cred.setBigDecimal(1, valor); cred.setInt(2, idConta); cred.executeUpdate();

                // debita investimento
                valorInvest = valorInvest.subtract(valor);
                PreparedStatement upInv = con.prepareStatement(
                    "UPDATE investimento SET valor = ?, ultima_att = CURRENT_TIMESTAMP WHERE usuario_id = ?");
                upInv.setBigDecimal(1, valorInvest); upInv.setInt(2, userId); upInv.executeUpdate();

                // transação
                PreparedStatement pt = con.prepareStatement(
                    "INSERT INTO transacao (conta_destino, tipo, valor) VALUES (?, 'resgate', ?)");
                pt.setInt(1, idConta); pt.setBigDecimal(2, valor); pt.executeUpdate();

                ses.setAttribute("msgInv", "Resgate realizado com sucesso!");
            }

            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            ses.setAttribute("erroInv", "Erro: " + e.getMessage());
        }

        resp.sendRedirect("investimento");
    }
}