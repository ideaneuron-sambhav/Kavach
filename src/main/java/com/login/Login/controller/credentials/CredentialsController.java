package com.login.Login.controller.credentials;

import com.login.Login.dto.Response;
import com.login.Login.dto.credentials.CredentialsRequest;
import com.login.Login.dto.credentials.CredentialsResponse;
import com.login.Login.dto.credentials.CredentialRevealResponse;
import com.login.Login.dto.credentials.UpdateCredentialsRequest;
import com.login.Login.dto.otp.OtpVerifyRequest;
import com.login.Login.service.credentials.CredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Response<Page<CredentialsResponse>>> listCredentials(@RequestParam(defaultValue = "") String search,
                                                                               @RequestParam(defaultValue = "0") int page,
                                                                               @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(credentialsService.listCredentials(search, page, size));
    }

    // Update credential
    @PutMapping("/update/{id}")
    public ResponseEntity<Response<CredentialsResponse>> updateCredential(
            @PathVariable Long id,  //CredentialsId
            @RequestBody UpdateCredentialsRequest updateCredentialsRequest) {
        CredentialsRequest request = updateCredentialsRequest.getCredentialsRequest();
        OtpVerifyRequest otpVerifyRequest = updateCredentialsRequest.getOtpVerifyRequest();
        return ResponseEntity.ok(credentialsService.updateCredential(id, request, otpVerifyRequest.getRefId(), otpVerifyRequest.getOtp()));
    }

    // Update credential password
    @PutMapping("/update/password/{id}")
    public ResponseEntity<Response<CredentialsResponse>> updateCredentialPassword(
            @PathVariable Long id,  //CredentialsId
            @RequestBody UpdateCredentialsRequest updateCredentialsRequest) {
        CredentialsRequest request = updateCredentialsRequest.getCredentialsRequest();
        OtpVerifyRequest otpVerifyRequest = updateCredentialsRequest.getOtpVerifyRequest();
        return ResponseEntity.ok(credentialsService.updatePassword(id, request, otpVerifyRequest.getRefId(), otpVerifyRequest.getOtp()));
    }

    // Toggle active/inactive
    @PutMapping("/toggle/{id}") //CredentialsId
    public ResponseEntity<Response<CredentialsResponse>> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(credentialsService.toggleActive(id));
    }

    // Generate OTP for password reveal
    @PostMapping("/otp/generate/password/{id}")
    public ResponseEntity<Response<Map<String,Object>>> generateOtpForPassword(@PathVariable Long id) {
        return ResponseEntity.ok(credentialsService.generateOtpForPassword(id));
    }
    @PostMapping("/otp/generate/update/password/{id}")
    public ResponseEntity<Response<Map<String,Object>>> generateOtpForUpdatePassword(@PathVariable Long id) {
        return ResponseEntity.ok(credentialsService.generateOtpForUpdatePassword(id));
    }
    @PostMapping("/otp/generate/update/{id}")
    public ResponseEntity<Response<Map<String,Object>>> generateOtpForUpdate(@PathVariable Long id) {
        return ResponseEntity.ok(credentialsService.generateOtpForUpdate(id));
    }

    // Reveal password using refId only
    @PostMapping("/otp/verify/password")
    public ResponseEntity<Response<CredentialRevealResponse>> revealPassword(@RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(credentialsService.revealPassword(request.getRefId(),request.getOtp()));
    }
}
