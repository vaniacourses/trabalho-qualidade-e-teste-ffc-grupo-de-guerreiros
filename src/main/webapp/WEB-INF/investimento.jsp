<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.math.BigDecimal" %>
<%
    // Verifica se o usuário está logado
    if (session == null || session.getAttribute("idUsuario") == null) {
        response.sendRedirect("login");
        return;
    }
    
    BigDecimal valorAtual = (BigDecimal) request.getAttribute("valorAtual");
    if (valorAtual == null) {
        valorAtual = BigDecimal.ZERO;
    }
    
    String msgInv = (String) session.getAttribute("msgInv");
    String erroInv = (String) session.getAttribute("erroInv");
    
    // Remove as mensagens da sessão após usar
    if (msgInv != null) session.removeAttribute("msgInv");
    if (erroInv != null) session.removeAttribute("erroInv");
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Investimento – Banco Campos</title>
    <style>
        :root{
            --bg:#0b1c2c;
            --accent:#d4af37;
            --card:#13273a;
            --text:#fff;
            --ok:#2ecc71;
            --erro:#e74c3c;
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
            background:var(--card);
            padding:40px 32px;
            border-radius:10px;
            box-shadow:0 10px 18px rgba(0,0,0,.4);
            width:90%;
            max-width:460px;
            text-align:center;
        }
        
        h2 {
            color:var(--accent);
            margin-bottom:12px;
        }
        
        .valor {
            font-size:28px;
            font-weight:bold;
            color:var(--accent);
            margin-bottom:20px;
        }
        
        form {
            margin:14px 0;
        }
        
        input {
            padding:10px;
            border-radius:6px;
            border:none;
            width:160px;
            margin-bottom:8px;
        }
        
        .btn {
            padding:10px 24px;
            border:none;
            border-radius:6px;
            font-weight:bold;
            cursor:pointer;
        }
        
        .investir {
            background:var(--accent);
            color:#000;
        }
        
        .retirar {
            background:crimson;
            color:#fff;
            margin-left:8px;
        }
        
        .btn:hover {
            filter:brightness(1.1);
        }
        
        .msg {
            margin-top:12px;
            font-weight:600;
        }
        
        a.link {
            display:inline-block;
            margin-top:25px;
            color:var(--accent);
            text-decoration:none;
        }
    </style>
</head>
<body>
    <div class="card">
        <h2>Poupança Campos</h2>
        <div class="valor">R$ <%= valorAtual.toPlainString() %></div>
        
        <% if (msgInv != null) { %>
            <div class="msg" style="color:var(--ok)"><%= msgInv %></div>
        <% } %>
        
        <% if (erroInv != null) { %>
            <div class="msg" style="color:var(--erro)"><%= erroInv %></div>
        <% } %>
        
        <form method="post" action="investimento">
            <input type="number" step="0.01" name="valor" placeholder="Valor" required><br>
            <button class="btn investir" name="op" value="investir">Investir</button>
            <button class="btn retirar" name="op" value="retirar">Retirar</button>
        </form>
        
        <a class="link" href="painel">&larr; Voltar ao Painel</a>
    </div>
</body>
</html>