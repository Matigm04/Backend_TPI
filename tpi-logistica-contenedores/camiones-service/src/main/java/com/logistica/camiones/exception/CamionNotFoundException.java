package com.logistica.camiones.exception;

public class CamionNotFoundException extends RuntimeException {
    public CamionNotFoundException(String message) {
        super(message);
    }
}
