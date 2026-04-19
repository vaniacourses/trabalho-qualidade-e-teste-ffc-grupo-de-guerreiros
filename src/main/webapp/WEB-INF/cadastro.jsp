<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cadastro – Banco Campos</title>
    <style>
        :root {
            --bg:#0b1c2c;
            --accent:#d4af37;
            --text:#fff;
            --muted:#cfd6e1;
        }
        
        * {
            box-sizing:border-box;
            margin:0;
            padding:0;
            font-family:'Segoe UI',Arial,sans-serif;
        }
        
        body {
            background:var(--bg);
            color:var(--text);
            min-height:100vh;
            display:flex;
            flex-direction:column;
        }
        
        header {
            display:flex;
            justify-content:space-between;
            align-items:center;
            padding:20px 8%;
            background:rgba(0,0,0,.3);
        }
        
        .logo {
            font-size:1.8rem;
            font-weight:700;
            color:var(--accent);
        }
        
        .btn {
            background:var(--accent);
            color:#000;
            padding:10px 20px;
            border:none;
            border-radius:6px;
            cursor:pointer;
            font-weight:600;
            transition:.3s;
        }
        
        .btn:hover {
            background:#b48f2e;
            transform:translateY(-2px);
        }
        
        .card {
            background:#13273a;
            padding:40px 32px;
            border-radius:10px;
            box-shadow:0 10px 18px rgba(0,0,0,.4);
            max-width:400px;
            margin:100px auto;
            width:90%;
        }
        
        h2 {
            text-align:center;
            margin-bottom:24px;
            color:var(--accent);
        }
        
        input {
            width:100%;
            padding:12px 14px;
            margin:10px 0;
            background:#0f2236;
            border:1px solid #334861;
            border-radius:6px;
            color:var(--text);
        }
        
        input::placeholder {
            color:var(--muted);
        }
        
        form button {
            width:100%;
            margin-top:14px;
        }
        
        a.link {
            display:block;
            text-align:center;
            color:var(--muted);
            margin-top:14px;
            text-decoration:none;
        }
        
        a.link:hover {
            color:var(--accent);
        }
    </style>
</head>
<body>
    <!-- HEADER -->
    <header>
        <div class="logo">Banco Campos</div>
        <button class="btn" onclick="location.href='login'">Login</button>
    </header>

    <!-- FORM -->
    <div class="card">
        <h2>Cadastro</h2>
        <form method="post" action="cadastro">
            <input type="text" name="nome" placeholder="Nome completo" required>
            <input type="email" name="email" placeholder="Email" required>
            <input type="password" name="senha" placeholder="Senha" required>
            <button class="btn" type="submit">Cadastrar</button>
        </form>
        <a class="link" href="menu">&larr; Voltar ao início</a>
    </div>
</body>
</html>