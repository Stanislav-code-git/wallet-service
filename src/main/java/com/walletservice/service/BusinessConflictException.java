package com.walletservice.service;

public class BusinessConflictException extends RuntimeException {
    public BusinessConflictException(String msg) { super(msg); }
}