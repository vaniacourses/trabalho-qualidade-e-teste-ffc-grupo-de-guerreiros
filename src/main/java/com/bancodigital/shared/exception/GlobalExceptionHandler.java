package com.bancodigital.shared.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public String handleDomain(DomainException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("erro", ex.getMessage());
        return "redirect:/painel";
    }

    @ExceptionHandler(Exception.class)
    public String handleAny(Exception ex, RedirectAttributes ra) {
        log.error("Unexpected error", ex);
        ra.addFlashAttribute("erro", "Erro inesperado. Tente novamente em instantes.");
        return "redirect:/painel";
    }
}
