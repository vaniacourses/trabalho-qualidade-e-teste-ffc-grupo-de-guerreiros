<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Verifica se o usuário está logado
    if (session == null || session.getAttribute("idUsuario") == null) {
        response.sendRedirect("login");
        return;
    }
    
    String numero = (String) request.getAttribute("numero");
    Double saldo = (Double) request.getAttribute("saldo");
    if (numero == null) numero = "";
    if (saldo == null) saldo = 0.0;
    
    String msgTransfer = (String) session.getAttribute("msgTransfer");
    String erroTransfer = (String) session.getAttribute("erroTransfer");
    
    // Remove as mensagens da sessão após usar
    if (msgTransfer != null) session.removeAttribute("msgTransfer");
    if (erroTransfer != null) session.removeAttribute("erroTransfer");
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Transferência – Banco Campos</title>
    <style>
        :root {
            --bg:#0b1c2c;
            --accent:#d4af37;
            --text:#fff;
            --muted:#cfd6e1;
            --erro:#e74c3c;
            --ok:#2ecc71;
        }
        
        * {
            box-sizing:border-box;
            margin:0;
            padding:0;
            font-family:'Segoe UI',sans-serif;
        }
        
        body {
            background:var(--bg);
            color:var(--text);
            display:flex;
            flex-direction:column;
            align-items:center;
            padding:40px;
        }
        
        .card {
            background:#13273a;
            padding:30px 40px;
            border-radius:10px;
            box-shadow:0 8px 18px rgba(0,0,0,.4);
            max-width:440px;
            width:90%;
        }
        
        h2 {
            color:var(--accent);
            margin-bottom:16px;
            text-align:center;
        }
        
        p {
            margin:6px 0;
            text-align:center;
        }
        
        .saldo {
            color:var(--ok);
            font-weight:bold;
        }
        
        form {
            margin-top:20px;
            display:flex;
            flex-direction:column;
            align-items:center;
        }
        
        input {
            margin:6px 0;
            padding:10px;
            border-radius:6px;
            border:none;
            width:80%;
            max-width:300px;
        }
        
        button {
            margin-top:14px;
            background:var(--accent);
            color:#000;
            border:none;
            padding:10px 24px;
            border-radius:6px;
            font-weight:bold;
            cursor:pointer;
        }
        
        button:hover {
            background:#f6d660;
        }
        
        .msg {
            margin-top:12px;
            font-weight:bold;
            text-align:center;
        }
        
        a {
            display:block;
            margin-top:20px;
            color:var(--muted);
            text-align:center;
            text-decoration:none;
        }
        
        a:hover {
            color:var(--accent);
        }
    </style>
</head>
<body>
    <div class="card">
        <h2>Transferência</h2>
        <p>Conta: <strong><%= numero %></strong></p>
        <p>Saldo atual: <span class="saldo">R$ <%= String.format("%.2f", saldo) %></span></p>

        <% if (msgTransfer != null) { %>
            <p class="msg" style="color:var(--ok)"><%= msgTransfer %></p>
        <% } %>

        <% if (erroTransfer != null) { %>
            <p class="msg" style="color:var(--erro)"><%= erroTransfer %></p>
        <% } %>

        <form method="post" action="transferencia">
            <input type="text" name="contaDestino" placeholder="Conta destino" required>
            <input type="number" step="0.01" name="valor" placeholder="Valor (R$)" required>
            <button type="submit">Transferir</button>
        </form>

        <a href="painel">&larr; Voltar ao Painel</a>
    </div>
</body>
</html>