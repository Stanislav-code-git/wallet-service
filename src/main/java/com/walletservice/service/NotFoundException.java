package com.walletservice.service;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String msg) { super(msg); }
}