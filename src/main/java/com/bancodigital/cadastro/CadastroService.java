package com.bancodigital.cadastro;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bancodigital.conta.ContaRepository;
import com.bancodigital.login.UsuarioRepository;
import com.bancodigital.shared.Mensagens;
import com.bancodigital.shared.exception.DomainException;

@Service
public class CadastroService {

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int SENHA_MIN = 8;

    private final UsuarioRepository usuarioRepository;
    private final ContaRepository contaRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastroService(UsuarioRepository usuarioRepository,
                           ContaRepository contaRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.contaRepository = contaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String validarCadastro(String nome, String email, String senha) {
        if (nome == null || nome.trim().isEmpty()) return Mensagens.NOME_INVALIDO;
        if (email == null || email.trim().isEmpty()) return Mensagens.EMAIL_INVALIDO;
        if (!EMAIL_REGEX.matcher(email.trim()).matches()) return Mensagens.EMAIL_INVALIDO;
        if (senha == null || senha.length() < SENHA_MIN) return Mensagens.SENHA_CURTA;
        return "OK";
    }

    @Transactional
    public void cadastrar(CadastroForm form) {
        String resultado = validarCadastro(form.getNome(), form.getEmail(), form.getSenha());
        if (!"OK".equals(resultado)) {
            throw new DomainException(resultado);
        }
        String email = form.getEmail().trim();
        if (usuarioRepository.existsByEmail(email)) {
            throw new DomainException(Mensagens.EMAIL_DUPLICADO);
        }
        String hash = passwordEncoder.encode(form.getSenha());
        long usuarioId = usuarioRepository.save(form.getNome().trim(), email, hash);
        String numero = contaRepository.proximoNumeroConta();
        contaRepository.inserir(numero, usuarioId);
    }
}
