package com.logistica.tarifas.exception;

public class TarifaNotFoundException extends RuntimeException {
    public TarifaNotFoundException(String message) {
        super(message);
    }
}
