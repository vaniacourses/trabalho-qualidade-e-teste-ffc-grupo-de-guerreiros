package com.mycompany.a;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "transferencia", urlPatterns = {"/transferencia"})
public class Transferencia extends HttpServlet {

    public String validarTransferencia(double valor, String numDestino, String numOrigem, double saldoOrigem, boolean destinoExiste) {
        if (valor <= 0 || numDestino == null || numDestino.isBlank()) {
            return "Valor ou conta inválidos.";
        }
        if (numDestino.equals(numOrigem)) {
            return "Não é possível transferir para a própria conta.";
        }
        if (valor > saldoOrigem) {
            return "Saldo insuficiente.";
        }
        if (!destinoExiste) {
            return "Conta destino não encontrada.";
        }
        return "OK";
    }

    

    /* ---------- TELA (GET) ---------- */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession ses = req.getSession(false);
        if (ses == null || ses.getAttribute("idUsuario") == null) {
            resp.sendRedirect("login");
            return;
        }

        int idUsuario = (int) ses.getAttribute("idUsuario");
        int idConta = 0;
        double saldo = 0;
        String numero = "";

        try (Connection con = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT id, numero, saldo FROM conta WHERE usuario_id = ?");
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                idConta = rs.getInt("id");
                numero = rs.getString("numero");
                saldo = rs.getDouble("saldo");
                ses.setAttribute("idConta", idConta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Passa dados para o JSP
        req.setAttribute("numero", numero);
        req.setAttribute("saldo", saldo);
        req.getRequestDispatcher("/WEB-INF/transferencia.jsp").forward(req, resp);
    }

    /* ---------- PROCESSA (POST) ---------- */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession ses = req.getSession(false);
        if (ses == null || ses.getAttribute("idUsuario") == null) {
            resp.sendRedirect("login");
            return;
        }

        int idContaOrigem = (int) ses.getAttribute("idConta");
        String numDestino = req.getParameter("contaDestino");
        double valor = 0;
        String redirectURL = "transferencia";

        try { valor = Double.parseDouble(req.getParameter("valor")); }
        catch (NumberFormatException ignore) {}

        if (valor <= 0 || numDestino == null || numDestino.isBlank()) {
            ses.setAttribute("erroTransfer", "Valor ou conta inválidos.");
            resp.sendRedirect(redirectURL);
            return;
        }

        Connection con = null;
        try {
            con = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());

            PreparedStatement psOrig = con.prepareStatement(
                "SELECT saldo, numero FROM conta WHERE id = ?");
            psOrig.setInt(1, idContaOrigem);
            ResultSet rsOrig = psOrig.executeQuery();
            if (!rsOrig.next()) throw new SQLException("Conta origem não encontrada.");

            double saldoOrigem = rsOrig.getDouble("saldo");
            String numOrigem   = rsOrig.getString("numero");

            if (numDestino.equals(numOrigem)) {
                ses.setAttribute("erroTransfer", "Não é possível transferir para a própria conta.");
                return;
            }
            if (valor > saldoOrigem) {
                ses.setAttribute("erroTransfer", "Saldo insuficiente.");
                return;
            }

            PreparedStatement psDest = con.prepareStatement(
                "SELECT id FROM conta WHERE numero = ?");
            psDest.setString(1, numDestino);
            ResultSet rsDest = psDest.executeQuery();
            if (!rsDest.next()) {
                ses.setAttribute("erroTransfer", "Conta destino não encontrada.");
                return;
            }
            int idContaDestino = rsDest.getInt("id");

            con.setAutoCommit(false);

            PreparedStatement deb = con.prepareStatement(
                "UPDATE conta SET saldo = saldo - ? WHERE id = ?");
            deb.setDouble(1, valor);
            deb.setInt(2, idContaOrigem);
            deb.executeUpdate();

            PreparedStatement cred = con.prepareStatement(
                "UPDATE conta SET saldo = saldo + ? WHERE id = ?");
            cred.setDouble(1, valor);
            cred.setInt(2, idContaDestino);
            cred.executeUpdate();

            PreparedStatement pt = con.prepareStatement(
                "INSERT INTO transacao (conta_origem, conta_destino, tipo, valor) "
              + "VALUES (?, ?, 'transferencia', ?)");
            pt.setInt(1, idContaOrigem);
            pt.setInt(2, idContaDestino);
            pt.setDouble(3, valor);
            pt.executeUpdate();

            con.commit();
            ses.setAttribute("msgTransfer", "Transferência realizada com sucesso!");

        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ig) {}
            e.printStackTrace();
            ses.setAttribute("erroTransfer", "Erro ao transferir: " + e.getMessage());
        } finally {
            if (con != null) try { con.close(); } catch (SQLException ig) {}
            resp.sendRedirect(redirectURL);
        }
    }
}