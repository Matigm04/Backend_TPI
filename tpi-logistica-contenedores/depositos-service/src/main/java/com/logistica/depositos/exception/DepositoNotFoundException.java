package com.logistica.depositos.exception;

public class DepositoNotFoundException extends RuntimeException {
    public DepositoNotFoundException(String message) {
        super(message);
    }
}
