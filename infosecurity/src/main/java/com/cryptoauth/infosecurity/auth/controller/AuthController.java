package com.cryptoauth.infosecurity.auth.controller;

import com.cryptoauth.infosecurity.auth.dto.*;
import com.cryptoauth.infosecurity.auth.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResult> loginAndSendOtp(@RequestBody UserLogin userLogin) {
        LoginResult result = authService.login(userLogin);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> repeatOtp(@RequestBody ResendOtpRequest request) {
        authService.repeatOtpCode(request);
        return ResponseEntity.ok("OTP repeat");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok("OTP sent to email");
    }

    @PostMapping("/verify-otp-forgot-password")
    public ResponseEntity<String> verifyOtpForgotPassword(@RequestBody OtpVerifyRequest request) {
        authService.verifyOtpForgotPassword(request);
        return ResponseEntity.ok("OTP verify success!");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("Password has been reset successfully");
    }

    @PostMapping("/token")
    public ResponseEntity<String> getToken(@RequestBody OtpVerifyRequest request) {
        String token = authService.verifyAndGenerateToken(request);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/valid-token")
    public ResponseEntity<String> validToken(@RequestParam("token") String token) {
        authService.validToken(token);
        return ResponseEntity.ok("Token is valid");
    }
}
