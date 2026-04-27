<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.mycompany.a.Extrato.Transacao" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Extrato – Banco Campos</title>
    <style>
        :root{--bg:#0b1c2c;--accent:#d4af37;--card:#13273a;--text:#fff;--muted:#cfd6e1}
        *{box-sizing:border-box;margin:0;padding:0;font-family:'Segoe UI',Arial,sans-serif}
        body{background:var(--bg);color:var(--text);display:flex;flex-direction:column;align-items:center;padding:40px}
        .container{max-width:700px;width:95%;background:var(--card);border-radius:10px;padding:30px;box-shadow:0 10px 18px rgba(0,0,0,.4)}
        h2{color:var(--accent);margin-bottom:20px;text-align:center}
        .scroll-area{max-height:500px;overflow-y:auto;padding-right:4px}
        .item{display:flex;background:#1e3248;margin-bottom:10px;border-radius:6px;box-shadow:0 1px 3px rgba(0,0,0,.2);overflow:hidden}
        .faixa{width:6px}
        .conteudo{padding:15px;flex:1}
        .valor{font-size:18px;font-weight:bold;margin-bottom:4px;color:var(--accent)}
        .desc{color:var(--muted);margin-bottom:3px}
        .data{font-size:12px;color:#aaaaaa}
        a.voltar{display:inline-block;margin-top:25px;padding:10px 20px;background:var(--accent);color:#000;text-decoration:none;border-radius:6px;font-weight:600}
        a.voltar:hover{filter:brightness(1.1)}
        ::-webkit-scrollbar{width:8px}
        ::-webkit-scrollbar-thumb{background:#555;border-radius:10px}
    </style>
</head>
<body>
    <div class="container">
        <h2>Extrato da Conta</h2>
        <div class="scroll-area">
            <%
                String erro = (String) request.getAttribute("erro");
                if (erro != null) {
            %>
                <p><%= erro %></p>
            <%
                } else {
                    List<Transacao> transacoes = (List<Transacao>) request.getAttribute("transacoes");
                    if (transacoes != null && !transacoes.isEmpty()) {
                        for (Transacao t : transacoes) {
            %>
                            <div class="item">
                                <div class="faixa" style="background:<%= t.getCor() %>"></div>
                                <div class="conteudo">
                                    <div class="valor">R$ <%= String.format("%.2f", t.getValor()) %></div>
                                    <div class="desc"><%= t.getDescricao() %></div>
                                    <div class="data"><%= t.getData().toString() %></div>
                                </div>
                            </div>
            <%
                        }
                    } else {
            %>
                        <p>Nenhuma transação encontrada.</p>
            <%
                    }
                }
            %>
        </div>
        <a href="painel" class="voltar">&larr; Voltar ao Painel</a>
    </div>
</body>
</html>