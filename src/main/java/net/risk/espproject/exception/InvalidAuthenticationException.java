package net.risk.espproject.exception;

import jakarta.servlet.ServletException;

public class InvalidAuthenticationException extends ServletException {

    public InvalidAuthenticationException(String message) {
        super(message);
    }
}
