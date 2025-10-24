package com.login.Login.service.credentials;

import com.login.Login.dto.Response;
import com.login.Login.dto.credentials.CredentialsRequest;
import com.login.Login.dto.credentials.CredentialsResponse;
import com.login.Login.dto.credentials.CredentialRevealResponse;
import com.login.Login.entity.Clients;
import com.login.Login.entity.Credentials;
import com.login.Login.repository.ClientRepository;
import com.login.Login.repository.CredentialsRepository;
import com.login.Login.security.JwtUtil;
import com.login.Login.service.otp.OtpEntry;
import com.login.Login.service.otp.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CredentialsService {

    private final CredentialsRepository credentialsRepository;
    private final ClientRepository clientRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    // Add new credential
    public Response<CredentialsResponse> addCredential(CredentialsRequest request) {
        jwtUtil.ensureAdminFromContext();
        Long clientId = request.getClientId();
        Clients client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));

        Credentials credential = Credentials.builder()
                .clients(client)
                .email(request.getEmail())
                .password(request.getPassword())
                .mobileNumber(request.getMobileNumber())
                .platformName(request.getPlatformName())
                .twoFA(request.getTwoFA())
                .twoFATypes(request.getTwoFATypes())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Credentials saved = credentialsRepository.save(credential);

        return Response.<CredentialsResponse>builder()
                .data(toResponse(saved))
                .httpStatusCode(HttpStatus.CREATED.value())
                .message("Credential created successfully")
                .build();
    }

    // List all credentials (password hidden)
    public Response<List<CredentialsResponse>> listCredentials() {
        jwtUtil.ensureAdminFromContext();
        List<CredentialsResponse> list = credentialsRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return Response.<List<CredentialsResponse>>builder()
                .data(list)
                .httpStatusCode(HttpStatus.OK.value())
                .message("Credentials fetched successfully (password hidden)")
                .build();
    }

    // Update credential
    @Transactional
    public Response<CredentialsResponse> updateCredential(Long id, CredentialsRequest request) {
        jwtUtil.ensureAdminFromContext();
        Credentials credential = credentialsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credential not found with ID: " + id));

        if (request.getEmail() != null) credential.setEmail(request.getEmail());
        if (request.getPassword() != null) credential.setPassword(request.getPassword());
        if (request.getMobileNumber() != null) credential.setMobileNumber(request.getMobileNumber());
        if (request.getPlatformName() != null) credential.setPlatformName(request.getPlatformName());
        if (request.getTwoFA() != null) credential.setTwoFA(request.getTwoFA());
        if (request.getTwoFATypes() != null) credential.setTwoFATypes(request.getTwoFATypes());
        if (request.getActive() != null) credential.setActive(request.getActive());

        Credentials updated = credentialsRepository.save(credential);

        return Response.<CredentialsResponse>builder()
                .data(toResponse(updated))
                .httpStatusCode(HttpStatus.OK.value())
                .message("Credential updated successfully")
                .build();
    }

    // Toggle active/inactive
    public Response<CredentialsResponse> toggleActive(Long id) {
        jwtUtil.ensureAdminFromContext();
        Credentials credential = credentialsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credential not found with ID: " + id));

        credential.setActive(!credential.getActive());
        Credentials updated = credentialsRepository.save(credential);

        String status = updated.getActive() ? "activated" : "deactivated";

        return Response.<CredentialsResponse>builder()
                .data(toResponse(updated))
                .httpStatusCode(HttpStatus.OK.value())
                .message("Credential " + status + " successfully")
                .build();
    }

    // Generate OTP for password reveal
    public Response<Map<String,Object>> generateOtpForPassword(Long credentialId) {
        jwtUtil.ensureAdminFromContext();
        Credentials credential = credentialsRepository.findById(credentialId)
                .orElseThrow(() -> new RuntimeException("Credential not found with ID: " + credentialId));

        var otpResponse = otpService.generateOtp(credential.getEmail());
        System.out.println("Generated OTP for " + credential.getEmail()+ ": " + otpResponse.getOtp());
        System.out.println("RefId: " + otpResponse.getRefId());

        Map<String, Object> result = new HashMap<>();
        result.put("refId", otpResponse.getRefId());

        return Response.<Map<String,Object>>builder()
                .data(result)
                .httpStatusCode(HttpStatus.OK.value())
                .message("OTP generated successfully for credential ID: " + credentialId)
                .build();
    }

    public Response<CredentialRevealResponse> revealPassword(String refId, String otp) {
        jwtUtil.ensureAdminFromContext();

        // Fetch OTP entry
        OtpEntry otpEntry = otpService.getOtpEntry(refId);

        // Check if OTP entry exists
        if (otpEntry == null || otpEntry.isExpired()) {
            return Response.<CredentialRevealResponse>builder()
                    .data(null)
                    .httpStatusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("Invalid or expired OTP")
                    .build();
        }

        // Verify the provided OTP
        boolean valid = otpService.verifyOtp(refId, otp);
        if (!valid) {
            return Response.<CredentialRevealResponse>builder()
                    .data(null)
                    .httpStatusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("Invalid OTP")
                    .build();
        }

        String email = otpEntry.getEmail();

        // Fetch credential by email
        Credentials credential = credentialsRepository.findAll().stream()
                .filter(c -> c.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Credential not found for email: " + email));

        CredentialRevealResponse response = CredentialRevealResponse.builder()
                .credentialId(credential.getId())
                .password(credential.getPassword())
                .build();

        return Response.<CredentialRevealResponse>builder()
                .data(response)
                .httpStatusCode(HttpStatus.OK.value())
                .message("Password revealed successfully")
                .build();
    }



    // Mapper: Entity -> Response DTO (without password)
    private CredentialsResponse toResponse(Credentials credential) {
        return CredentialsResponse.builder()
                .id(credential.getId())
                .clients(credential.getClients())
                .email(credential.getEmail())
                .mobileNumber(credential.getMobileNumber())
                .platformName(credential.getPlatformName())
                .twoFA(credential.getTwoFA())
                .twoFATypes(credential.getTwoFATypes())
                .active(credential.getActive())
                .createdAt(credential.getCreatedAt())
                .updatedAt(credential.getUpdatedAt())
                .build();
    }
}
