<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Saldo – Banco Campos</title>
    <style>
        :root{--bg:#0b1c2c;--accent:#d4af37;--text:#fff;--muted:#cfd6e1}
        *{box-sizing:border-box;margin:0;padding:0;font-family:'Segoe UI',Arial,sans-serif}
        body{background:var(--bg);color:var(--text);display:flex;flex-direction:column;min-height:100vh}
        header{display:flex;justify-content:space-between;align-items:center;padding:20px 8%;background:rgba(0,0,0,.3)}
        .logo{font-size:1.8rem;font-weight:700;color:var(--accent)}
        .btn-top{background:var(--accent);color:#000;padding:8px 18px;border:none;border-radius:6px;font-weight:600;cursor:pointer}
        .card{background:#13273a;padding:40px 32px;border-radius:10px;box-shadow:0 10px 18px rgba(0,0,0,.4);width:90%;max-width:420px;margin:80px auto;text-align:center}
        .card h2{margin-bottom:10px;color:var(--accent)}
        .card h3{margin-bottom:8px;color:var(--muted)}
        .saldo{font-size:28px;font-weight:bold;color:#2ecc71;margin-bottom:20px}
        a.link{display:block;text-align:center;color:var(--muted);margin-top:20px;text-decoration:none}
        a.link:hover{color:var(--accent)}
    </style>
</head>
<body>
    <header>
        <div class="logo">Banco Campos</div>
        <button class="btn-top" onclick="location.href='painel'">Painel</button>
    </header>

    <div class="card">
        <h2>Conta: <%= request.getAttribute("numeroConta") %></h2>
        <h3>Saldo disponível:</h3>
        <div class="saldo">R$ <%= String.format("%.2f", (Double) request.getAttribute("saldo")) %></div>
        <a class="link" href="painel">&larr; Voltar ao Painel</a>
    </div>
</body>
</html>