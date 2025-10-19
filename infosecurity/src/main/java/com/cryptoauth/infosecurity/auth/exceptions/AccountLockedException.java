package com.cryptoauth.infosecurity.auth.exceptions;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException(String email) {
        super("Account locked: " + email);
    }
    
}
