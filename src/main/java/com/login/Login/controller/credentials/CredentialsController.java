package com.login.Login.controller.credentials;

import com.login.Login.dto.Response;
import com.login.Login.dto.credentials.CredentialsRequest;
import com.login.Login.dto.credentials.CredentialsResponse;
import com.login.Login.dto.credentials.CredentialRevealResponse;
import com.login.Login.dto.otp.OtpVerifyRequest;
import com.login.Login.service.credentials.CredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/credentials")
@RequiredArgsConstructor
public class CredentialsController {

    @Autowired
    CredentialsService credentialsService;

    // Add new credential
    @PostMapping("/add")
    public ResponseEntity<Response<CredentialsResponse>> addCredential(
            @RequestBody CredentialsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(credentialsService.addCredential(request));
    }

    // List all credentials (password hidden)
    @GetMapping("/list")
    public ResponseEntity<Response<List<CredentialsResponse>>> listCredentials() {
        return ResponseEntity.ok(credentialsService.listCredentials());
    }

    // Update credential
    @PutMapping("/update/{id}")
    public ResponseEntity<Response<CredentialsResponse>> updateCredential(
            @PathVariable Long id,  //CredentialsId
            @RequestBody CredentialsRequest request) {
        return ResponseEntity.ok(credentialsService.updateCredential(id, request));
    }

    // Toggle active/inactive
    @PutMapping("/toggle/{id}") //CredentialsId
    public ResponseEntity<Response<CredentialsResponse>> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(credentialsService.toggleActive(id));
    }

    // Generate OTP for password reveal
    @PostMapping("/otp/generate/{id}")
    public ResponseEntity<Response<Map<String,Object>>> generateOtp(@PathVariable Long id) {
        return ResponseEntity.ok(credentialsService.generateOtpForPassword(id));
    }

    // Reveal password using refId only
    @PostMapping("/verify-otp")
    public ResponseEntity<Response<CredentialRevealResponse>> revealPassword(@RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(credentialsService.revealPassword(request.getRefId(),request.getOtp()));
    }
}
