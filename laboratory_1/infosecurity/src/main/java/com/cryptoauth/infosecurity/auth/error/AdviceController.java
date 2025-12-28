package com.cryptoauth.infosecurity.auth.error;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class AdviceController implements AuthenticationEntryPoint {
    private final Logger log = LoggerFactory.getLogger(AdviceController.class);

    public static ErrorCauseDto getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        final StackTraceElement firstError = rootCause.getStackTrace()[0];
        return new ErrorCauseDto(
                rootCause.getClass().getName(),
                firstError.getMethodName(),
                firstError.getFileName(),
                firstError.getLineNumber());
    }

    private ResponseEntity<ErrorDto> handleException(Throwable throwable, HttpStatusCode httpCode) {
        log.error("{}", throwable.getMessage());
        throwable.printStackTrace();
        final ErrorDto errorDto = new ErrorDto(throwable.getMessage(), AdviceController.getRootCause(throwable));
        return new ResponseEntity<>(errorDto, httpCode);
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        final ResponseEntity<ErrorDto> body = handleException(authException, HttpStatus.UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(body.getStatusCode().value());
        response.getWriter().write(new ObjectMapper().writeValueAsString(body.getBody()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorDto> handleAccessDeniedException(Throwable throwable) {
        return handleException(throwable, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorDto> handleNotFoundException(Throwable throwable) {
        return handleException(throwable, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDto> handleDataIntegrityViolationException(Throwable throwable) {
        return handleException(throwable, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorDto> handleAnyException(Throwable throwable) {
        return handleException(throwable, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

