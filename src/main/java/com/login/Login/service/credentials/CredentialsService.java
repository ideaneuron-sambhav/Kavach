package com.login.Login.service.credentials;

import com.login.Login.dto.Response;
import com.login.Login.dto.credentials.CredentialsRequest;
import com.login.Login.dto.credentials.CredentialsResponse;
import com.login.Login.dto.credentials.CredentialRevealResponse;
import com.login.Login.entity.Clients;
import com.login.Login.entity.Credentials;
import com.login.Login.entity.User;
import com.login.Login.repository.ClientRepository;
import com.login.Login.repository.CredentialsRepository;
import com.login.Login.security.JwtUtil;
import com.login.Login.service.otp.OtpEntry;
import com.login.Login.service.otp.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CredentialsService {

    private final CredentialsRepository credentialsRepository;
    private final ClientRepository clientRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    // Add new credential
    public Response<CredentialsResponse> addCredential(CredentialsRequest request) {
        jwtUtil.ensureAdminFromContext(); // Only admin can create credentials
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
    public Response<Page<CredentialsResponse>> listCredentials(String keyword, int page, int size) {
        User user = jwtUtil.getAuthenticatedUserFromContext();

        Pageable pageable = PageRequest.of(page, size, Sort.by("platformName").ascending());

        Page<Credentials> credentialsPage;

        if (jwtUtil.isAdminFromContext()){
            credentialsPage = credentialsRepository.searchAllCredentials(keyword != null ? keyword : "", pageable);
        }else{
            credentialsPage = credentialsRepository.searchAssignedCredentials(user, keyword != null ? keyword : "", pageable);
        }

        Page<CredentialsResponse> response = credentialsPage.map(this::toResponse);


        return Response.<Page<CredentialsResponse>>builder()
                .data(response)
                .httpStatusCode(HttpStatus.OK.value())
                .message("Credentials fetched successfully")
                .build();
    }

    // Update credential
    @Transactional
    public Response<CredentialsResponse> updateCredential(Long id, CredentialsRequest request) {
        Credentials credential = credentialsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credential not found with ID: " + id));

        ensureAccess(credential); // Check if current user can update

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
        Credentials credential = credentialsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credential not found with ID: " + id));

        ensureAccess(credential); // Check if current user can toggle

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
        Credentials credential = credentialsRepository.findById(credentialId)
                .orElseThrow(() -> new RuntimeException("Credential not found with ID: " + credentialId));

        ensureAccess(credential); // Only admin/assigned user can generate OTP

        var otpResponse = otpService.generateOtp(credential.getEmail());
        Map<String, Object> result = new HashMap<>();
        result.put("refId", otpResponse.getRefId());

        return Response.<Map<String,Object>>builder()
                .data(result)
                .httpStatusCode(HttpStatus.OK.value())
                .message("OTP generated successfully for credential ID: " + credentialId)
                .build();
    }

    // Reveal password
    public Response<CredentialRevealResponse> revealPassword(String refId, String otp) {
        OtpEntry otpEntry = otpService.getOtpEntry(refId);

        if (otpEntry == null || otpEntry.isExpired()) {
            return Response.<CredentialRevealResponse>builder()
                    .data(null)
                    .httpStatusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("Invalid or expired OTP")
                    .build();
        }

        boolean valid = otpService.verifyOtp(refId, otp);
        if (!valid) {
            return Response.<CredentialRevealResponse>builder()
                    .data(null)
                    .httpStatusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("Invalid OTP")
                    .build();
        }

        String email = otpEntry.getEmail();
        Credentials credential = credentialsRepository.findAll().stream()
                .filter(c -> c.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Credential not found for email: " + email));

        ensureAccess(credential); // Only admin/assigned user can reveal password

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

    private void ensureAccess(Credentials credential) {
        boolean isAdmin = jwtUtil.isAdminFromContext(); // implement in JwtUtil
        Long currentUserId = jwtUtil.getUserIdFromContext(); // implement in JwtUtil

        if (isAdmin) return; // admin has full access

        if (credential.getClients().getAssignedUser() == null ||
                !credential.getClients().getAssignedUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Access denied: Not allowed to view or modify this credential");
        }
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
