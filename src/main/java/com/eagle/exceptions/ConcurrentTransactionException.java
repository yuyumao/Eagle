package com.eagle.exceptions;

public class ConcurrentTransactionException extends RuntimeException {
    public ConcurrentTransactionException(String message) {
        super(message);
    }
}