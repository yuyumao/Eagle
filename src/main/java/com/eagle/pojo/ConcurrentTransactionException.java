package com.eagle.pojo;

public class ConcurrentTransactionException extends RuntimeException {
    public ConcurrentTransactionException(String message) {
        super(message);
    }
}