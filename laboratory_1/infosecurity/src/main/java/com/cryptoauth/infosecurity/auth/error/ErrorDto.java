package com.cryptoauth.infosecurity.auth.error;

public class ErrorDto {
    private String error;
    private ErrorCauseDto rootCause;

    public ErrorDto(String error, ErrorCauseDto rootCause) {
        this.error = error;
        this.rootCause = rootCause;
    }

    public String getError() {
        return error;
    }

    public ErrorCauseDto getRootCause() {
        return rootCause;
    }
}