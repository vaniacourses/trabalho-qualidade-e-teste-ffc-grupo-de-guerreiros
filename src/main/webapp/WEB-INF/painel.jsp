<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Painel – Banco Campos</title>
    <style>
        :root {
            --bg: #0b1c2c;
            --accent: #d4af37;
            --text: #fff;
            --muted: #cfd6e1;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Segoe UI', Arial, sans-serif;
        }

        body {
            background: var(--bg);
            color: var(--text);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }

        header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px 8%;
            background: rgba(0, 0, 0, .3);
        }

        .logo {
            font-size: 1.8rem;
            font-weight: 700;
            color: var(--accent);
        }

        .btn {
            background: var(--accent);
            color: #000;
            padding: 10px 20px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-weight: 600;
            transition: .3s;
        }

        .btn:hover {
            background: #b48f2e;
            transform: translateY(-2px);
        }

        main {
            padding: 40px 8%;
            flex: 1;
        }

        h1 {
            margin-bottom: 10px;
        }

        h3 {
            margin-bottom: 30px;
            color: var(--muted);
        }

        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
        }

        .card {
            background: #13273a;
            border-left: 6px solid var(--accent);
            padding: 24px 20px;
            border-radius: 8px;
            transition: .3s;
            cursor: pointer;
            text-decoration: none;
            color: var(--text);
        }

        .card:hover {
            transform: scale(1.03);
            background: #1b334d;
        }

        .card span {
            font-size: 2rem;
            display: block;
            margin-bottom: 8px;
        }

        .logout {
            text-align: right;
            margin-top: 30px;
        }
    </style>
</head>
<body>
    <header>
        <div class="logo">Banco Campos</div>
        <form method="get" action="painel">
            <button class="btn" name="acao" value="logout">Logout</button>
        </form>
    </header>

    <main>
        <h1>Bem-vindo, ${nomeUsuario} 👋</h1>
        <h3>Escolha uma operação:</h3>

        <div class="grid">
            <a href="deposito" class="card">
                <span>➕</span>
                Depositar
            </a>

            <a href="saque" class="card">
                <span>➖</span>
                Sacar
            </a>

            <a href="transferencia" class="card">
                <span>🔁</span>
                Transferir
            </a>

            <a href="investimento" class="card">
                <span>📈</span>
                Investir
            </a>

            <a href="extrato" class="card">
                <span>📜</span>
                Ver Extrato
            </a>

            <a href="saldo" class="card">
                <span>💰</span>
                Ver Saldo
            </a>
        </div>
    </main>
</body>
</html>