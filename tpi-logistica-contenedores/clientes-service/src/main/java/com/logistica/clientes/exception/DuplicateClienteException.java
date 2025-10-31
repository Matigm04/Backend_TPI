package com.logistica.clientes.exception;

public class DuplicateClienteException extends RuntimeException {
    public DuplicateClienteException(String message) {
        super(message);
    }
}
