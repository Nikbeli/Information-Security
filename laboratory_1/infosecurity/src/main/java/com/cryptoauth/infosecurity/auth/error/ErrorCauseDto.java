package com.cryptoauth.infosecurity.auth.error;

public class ErrorCauseDto {
        private String exception;
    private String methodName;
    private String fineName;
    private int lineNumber;

    ErrorCauseDto(String exception, String methodName, String fineName, int lineNumber) {
        this.exception = exception;
        this.methodName = methodName;
        this.fineName = fineName;
        this.lineNumber = lineNumber;
    }

    public String getException() {
        return exception;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getFineName() {
        return fineName;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
