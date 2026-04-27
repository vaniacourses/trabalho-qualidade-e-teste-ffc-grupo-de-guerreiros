package com.mycompany.a;

import java.io.IOException;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(name = "saldo", urlPatterns = {"/saldo"})
public class Saldo extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessao = request.getSession(false);
        if (sessao == null || sessao.getAttribute("idUsuario") == null) {
            response.sendRedirect("login");
            return;
        }

        int idUsuario = (int) sessao.getAttribute("idUsuario");
        double saldo = 0;
        String numeroConta = "";

        try (Connection con = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())) {
            String sql = "SELECT numero, saldo FROM conta WHERE usuario_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, idUsuario);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                numeroConta = rs.getString("numero");
                saldo = rs.getDouble("saldo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Passa dados para o JSP
        request.setAttribute("numeroConta", numeroConta);
        request.setAttribute("saldo", saldo);
        
        // Redireciona para o JSP
        request.getRequestDispatcher("/WEB-INF/saldo.jsp").forward(request, response);
    }
}