package com.logistica.solicitudes.exception;

public class SolicitudNotFoundException extends RuntimeException {
    public SolicitudNotFoundException(String message) {
        super(message);
    }
}
