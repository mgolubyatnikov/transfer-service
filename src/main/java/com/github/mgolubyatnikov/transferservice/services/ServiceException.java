package com.github.mgolubyatnikov.transferservice.services;

public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}
