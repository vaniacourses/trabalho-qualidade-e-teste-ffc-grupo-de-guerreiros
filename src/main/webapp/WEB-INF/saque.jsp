<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Saque – Banco Campos</title>
    <style>
        :root{--bg:#0b1c2c;--accent:#d4af37;--text:#fff;--muted:#cfd6e1;--btn:#007bff}
        *{box-sizing:border-box;margin:0;padding:0;font-family:'Segoe UI',Arial,sans-serif}
        body{background:var(--bg);color:var(--text);display:flex;flex-direction:column;min-height:100vh}
        header{display:flex;justify-content:space-between;align-items:center;padding:20px 8%;background:rgba(0,0,0,.3)}
        .logo{font-size:1.8rem;font-weight:700;color:var(--accent)}
        .btn-top{background:var(--accent);color:#000;padding:8px 18px;border:none;border-radius:6px;font-weight:600;cursor:pointer}
        .card{background:#13273a;padding:40px 32px;border-radius:10px;box-shadow:0 10px 18px rgba(0,0,0,.4);width:90%;max-width:420px;margin:80px auto}
        .card h2{margin-bottom:6px;color:var(--accent)}
        .card p{margin-bottom:20px;color:var(--muted)}
        input{width:100%;padding:12px;margin:8px 0;background:#0f2236;border:1px solid #334861;border-radius:6px;color:var(--text)}
        input::placeholder{color:var(--muted)}
        .valor-btn{margin:4px;padding:8px 16px;border:1px solid var(--btn);border-radius:6px;background:#e9f1ff;color:#000;cursor:pointer}
        .main-btn{width:100%;margin-top:16px;background:crimson;color:#fff;border:none;border-radius:6px;padding:12px;font-weight:600;cursor:pointer}
        .main-btn:hover{filter:brightness(1.1)}
        a.link{display:block;text-align:center;color:var(--muted);margin-top:20px;text-decoration:none}
        a.link:hover{color:var(--accent)}
    </style>
    <script>
        function setValor(v){
            document.getElementById('valor').value=v;
        }
    </script>
</head>
<body>
    <header>
        <div class="logo">Banco Campos</div>
        <button class="btn-top" onclick="location.href='painel'">Painel</button>
    </header>

    <div class="card">
        <h2>Conta <%= request.getAttribute("conta") %></h2>
        <p>Saldo atual: <strong>R$ <%= String.format("%.2f", (Double) request.getAttribute("saldo")) %></strong></p>

        <%
            String msg = (String) session.getAttribute("msgSaque");
            if (msg != null) {
                boolean sucesso = msg.toLowerCase().contains("sucesso");
                String cor = sucesso ? "#3399ff" : "#e74c3c";
        %>
            <p style="color:<%= cor %>;font-weight:600"><%= msg %></p>
        <%
                session.removeAttribute("msgSaque");
            }
        %>

        <form method="post" action="saque">
            <input type="number" step="0.01" name="valor" id="valor" placeholder="Digite o valor">
            <div>
                <button type="button" class="valor-btn" onclick="setValor(10)">10</button>
                <button type="button" class="valor-btn" onclick="setValor(50)">50</button>
                <button type="button" class="valor-btn" onclick="setValor(100)">100</button>
                <button type="button" class="valor-btn" onclick="setValor(1000)">1000</button>
            </div>
            <button class="main-btn" type="submit">Sacar</button>
        </form>
        <a class="link" href="painel">&larr; Voltar ao Painel</a>
    </div>
</body>
</html>