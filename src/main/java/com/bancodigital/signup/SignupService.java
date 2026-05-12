package com.bancodigital.signup;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bancodigital.account.AccountRepository;
import com.bancodigital.auth.UserRepository;
import com.bancodigital.shared.Messages;
import com.bancodigital.shared.exception.DomainException;

@Service
public class SignupService {

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int PASSWORD_MIN = 8;

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupService(UserRepository userRepository,
                         AccountRepository accountRepository,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String validateSignup(String name, String email, String password) {
        if (name == null || name.trim().isEmpty()) return Messages.INVALID_NAME;
        if (email == null || email.trim().isEmpty()) return Messages.INVALID_EMAIL;
        if (!EMAIL_REGEX.matcher(email.trim()).matches()) return Messages.INVALID_EMAIL;
        if (password == null || password.length() < PASSWORD_MIN) return Messages.PASSWORD_TOO_SHORT;
        return "OK";
    }

    @Transactional
    public void register(SignupForm form) {
        String result = validateSignup(form.getName(), form.getEmail(), form.getPassword());
        if (!"OK".equals(result)) {
            throw new DomainException(result);
        }
        String email = form.getEmail().trim();
        if (userRepository.existsByEmail(email)) {
            throw new DomainException(Messages.DUPLICATE_EMAIL);
        }
        String hash = passwordEncoder.encode(form.getPassword());
        long userId = userRepository.save(form.getName().trim(), email, hash);
        String number = accountRepository.nextAccountNumber();
        accountRepository.insert(number, userId);
    }
}
