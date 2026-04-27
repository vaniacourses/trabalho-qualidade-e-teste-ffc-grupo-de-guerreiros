package com.mycompany.a;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(name = "extrato", urlPatterns = {"/extrato"})
public class Extrato extends HttpServlet {

    public static String corPorTipo(String tipo) {
        switch (tipo) {
            case "deposito": return "#3bb54a";
            case "saque": return "#e74c3c";
            case "transferencia": return "#3498db";
            case "investimento": return "#9b59b6";
            default: return "#999";
        }
    }

    public static String descricaoPorTipo(String tipo, int contaDestino) {
        switch (tipo) {
            case "deposito": return "Depósito realizado";
            case "saque": return "Saque efetuado";
            case "transferencia": return "Transferência para conta " + contaDestino;
            case "investimento": return "Investimento aplicado";
            default: return "Outro tipo";
        }
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
        int idConta = 0;

        // Obter o ID da conta
        try (Connection con = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())) {
            PreparedStatement ps = con.prepareStatement("SELECT id FROM conta WHERE usuario_id = ?");
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                idConta = rs.getInt("id");
            } else {
                throw new SQLException("Conta não encontrada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Pega transações
        List<Transacao> transacoes = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())) {
            String sql = "SELECT * FROM transacao WHERE conta_origem = ? OR conta_destino = ? ORDER BY data DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idConta);
            ps.setInt(2, idConta);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String tipo = rs.getString("tipo");
                double valor = rs.getDouble("valor");
                Timestamp data = rs.getTimestamp("data");
                int origem = rs.getInt("conta_origem");
                int destino = rs.getInt("conta_destino");

                String cor = corPorTipo(tipo);
                String descricao = descricaoPorTipo(tipo, destino);

                transacoes.add(new Transacao(tipo, valor, data, origem, destino, cor, descricao));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("erro", "Erro ao carregar transações.");
        }

        // Passa dados para o JSP
        req.setAttribute("transacoes", transacoes);
        
        // Redireciona para o JSP
        req.getRequestDispatcher("/WEB-INF/extrato.jsp").forward(req, resp);
    }

    // Classe interna para representar uma transação
    public static class Transacao {
        private String tipo;
        private double valor;
        private Timestamp data;
        private int origem;
        private int destino;
        private String cor;
        private String descricao;

        public Transacao(String tipo, double valor, Timestamp data, int origem, int destino, String cor, String descricao) {
            this.tipo = tipo;
            this.valor = valor;
            this.data = data;
            this.origem = origem;
            this.destino = destino;
            this.cor = cor;
            this.descricao = descricao;
        }

        // Getters
        public String getTipo() { return tipo; }
        public double getValor() { return valor; }
        public Timestamp getData() { return data; }
        public int getOrigem() { return origem; }
        public int getDestino() { return destino; }
        public String getCor() { return cor; }
        public String getDescricao() { return descricao; }
    }
}