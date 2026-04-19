<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang='pt-br'>
<head>
    <meta charset='UTF-8' />
    <meta name='viewport' content='width=device-width, initial-scale=1.0' />
    <title>Banco Campos – Inovação e Confiança</title>
    <style>
        :root {
            --bg-primary: #0b1c2c;
            --accent: #d4af37;
            --text-light: #ffffff;
            --text-muted: #cfd6e1;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Arial, sans-serif; }
        body { background: var(--bg-primary); color: var(--text-light); line-height: 1.6; }
        header { display: flex; align-items: center; justify-content: space-between; padding: 20px 8%; background: rgba(0,0,0,0.3); backdrop-filter: blur(4px); }
        .logo { font-size: 1.8rem; font-weight: 700; color: var(--accent); letter-spacing: 1px; }
        nav a { color: var(--text-light); margin-left: 24px; text-decoration: none; position: relative; }
        nav a::after { content: ''; position: absolute; left: 0; bottom: -4px; width: 0; height: 2px; background: var(--accent); transition: width .3s; }
        nav a:hover::after { width: 100%; }
        .btn { background: var(--accent); color: #000; padding: 10px 20px; border: none; border-radius: 6px; cursor: pointer; margin-left: 12px; font-weight: 600; transition: transform .2s, box-shadow .2s; }
        .btn:hover { transform: translateY(-2px); box-shadow: 0 4px 8px rgba(0,0,0,.4); }
        .hero { text-align: center; padding: 120px 6% 80px; background: linear-gradient(135deg, rgba(212,175,55,0.12) 0%, rgba(11,28,44,0.9) 60%); }
        .hero h1 { font-size: clamp(2.3rem, 6vw, 3.8rem); margin-bottom: 16px; }
        .hero p { font-size: 1.1rem; color: var(--text-muted); max-width: 700px; margin: 0 auto; }
        section { padding: 80px 8%; }
        section h2 { color: var(--accent); font-size: 2rem; margin-bottom: 20px; text-align: center; }
        .sobre p { max-width: 800px; margin: 0 auto; text-align: justify; }
        .clientes-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px,1fr)); gap: 24px; margin-top: 40px; }
        .card { background: #13273a; padding: 24px; border-radius: 8px; position: relative; transition: transform .3s, box-shadow .3s; }
        .card:hover { transform: translateY(-6px); box-shadow: 0 10px 18px rgba(0,0,0,.5); }
        .card::before { content: ''; position: absolute; inset: 0; border-radius: 8px; background: linear-gradient(135deg, transparent 60%, var(--accent) 100%); opacity: 0; transition: opacity .4s; }
        .card:hover::before { opacity: 0.12; }
        .card h3 { margin-bottom: 8px; color: var(--accent); }
        .card p { color: var(--text-muted); font-size: .9rem; }
        footer { background: #09141f; padding: 40px 8%; display: grid; gap: 20px; }
        footer h4 { color: var(--accent); margin-bottom: 10px; }
        footer a { color: var(--text-muted); text-decoration: none; }
        footer a:hover { color: var(--accent); }
        @media (max-width: 560px) { nav { display: none; } header { justify-content: space-between; } }
    </style>
</head>
<body>

    <header>
        <div class='logo'>Banco Campos</div>
        <nav>
            <a href='#sobre'>Quem S2omos</a>
            <a href='#clientes'>Clientes</a>
            <a href='login'>Login</a>
            <button class='btn' onclick="location.href='cadastro'">Sign‑up</button>
        </nav>
    </header>

    <section class='hero'>
        <h1>Bem‑vindo ao futuro das finanças</h1>
        <p>O Banco Campos combina tecnologia de ponta com atendimento humano para abrir as portas da prosperidade. Controle suas contas, investimentos e conquiste seus objetivos sem complicação.</p>
    </section>

    <section id='sobre' class='sobre'>
        <h2>Quem Somos</h2>
        <p>
            Fundado em 2025, o <strong>Banco Campos</strong> nasceu da visão de criar uma instituição que unisse <em>inovação</em> e <em>confiança</em>. Em um mundo em constante mudança digital, percebemos que as pessoas precisavam de um parceiro financeiro que fosse tão ágil quanto seguro. Orgulhamo‑nos de oferecer soluções bancárias 100% online, sem abrir mão do calor humano: nossa equipe está sempre pronta para ajudar você a transformar planos em conquistas.
        </p>
    </section>

    <section id='clientes' class='clientes'>
        <h2>Clientes</h2>
        <div class='clientes-grid'>

            <div class='card'>
                <h3>Turma de Introdução a Desenvolvimento Web A1</h3>
                <p>Parceiros desde 2025, aplicam nossos serviços para projetos acadêmicos que exploram desenvolvimento full‑stack e boas práticas.</p>
            </div>

            <div class='card'>
                <h3>StartUp Kinetix</h3>
                <p>Fintech de pagamentos instantâneos que confiou ao Banco Campos a gestão de tesouraria e investimentos de curto prazo.</p>
            </div>

            <div class='card'>
                <h3>Café Aurora</h3>
                <p>Rede de cafeterias artesanais que usa nossas APIs para conciliar vendas e fluxo de caixa em tempo real.</p>
            </div>

            <div class='card'>
                <h3>Associação Veleiros do Atlântico</h3>
                <p>Organização esportiva que encontrou no Banco Campos o suporte perfeito para patrocínios e eventos internacionais.</p>
            </div>

        </div>
    </section>

    <footer>
        <div>
            <h4>Contato</h4>
            <p>Tel: (21) 4002‑8922</p>
            <p>Email: contato@bancocampos.com</p>
        </div>
        <div>
            <h4>Endereço</h4>
            <p>Av. Jornalista Alberto Francisco Torres, Praia de Icaraí, Niterói – RJ, Brasil</p>
        </div>
        <div>
            <h4>Links Rápidos</h4>
            <a href='#sobre'>Quem Somos</a><br />
            <a href='#clientes'>Clientes</a><br />
            <a href='menu'>Menu Inicial</a>
        </div>
    </footer>

</body>
</html>