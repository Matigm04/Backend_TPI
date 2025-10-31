package com.logistica.camiones.exception;

public class DominioYaExisteException extends RuntimeException {
    public DominioYaExisteException(String message) {
        super(message);
    }
}
