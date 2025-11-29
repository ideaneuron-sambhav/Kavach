/*
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CredentialsService {
    @Autowired
    CredentialsRepository credentialsRepository;
    @Autowired
    ClientRepository clientRepository;
    @Autowired
    OtpService otpService;
    @Autowired
    JwtUtil jwtUtil;

    // Add new credential
    public Response<CredentialsResponse> addCredential(CredentialsRequest request) {
        jwtUtil.ensureAdminFromContext();
        Long clientId = request.getClientId();
        Clients client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));
        if(!client.getActive()){
            throw new RuntimeException("Client is inactive: "+client.getFirstName() + " " + client.getLastName());
        }
        Credentials credential = Credentials.builder()
                .clients(client)
                .userName(request.getUserName())
                .mobileNumber(request.getMobileNumber())
                .platformName(request.getPlatformName())
                .details(request.getDetails())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Credentials saved = credentialsRepository.save(credential);

        return Response.<CredentialsResponse>builder()
                .data(toResponse(saved))
                .httpStatusCode(HttpStatus.CREATED.value())
                .message("Credential created successfully")
                .build();
    }

    // List all credentials (Details hidden)
    public Response<Page<CredentialsResponse>> listCredentials(String keyword, int page, int size) {
        User user = jwtUtil.getAuthenticatedUserFromContext();

        Pageable pageable = PageRequest.of(page, size, Sort.by("platformName").ascending());

        Page<Credentials> credentialsPage;

        String searchKeyword = (keyword != null) ? keyword : "";

        if (jwtUtil.isAdminFromContext()) {
            credentialsPage = credentialsRepository.searchByMaskedForAdmin(searchKeyword, pageable);
        } else {
            credentialsPage = credentialsRepository.searchByMaskedForUser(user, searchKeyword, pageable);
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
    public Response<CredentialsResponse> updateCredential(Long id, CredentialsRequest request, String refId, String otp) {
        OtpEntry otpEntry = otpService.getOtpEntry(refId);

        if (otpEntry == null || otpEntry.isExpired()) {
            return Response.<CredentialsResponse>builder()
                    .data(null)
                    .httpStatusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("Invalid or expired OTP")
                    .build();
        }

        boolean valid = otpService.verifyOtp(refId, otp);
        if (!valid) {
            return Response.<CredentialsResponse>builder()
                    .data(null)
                    .httpStatusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("Invalid OTP")
                    .build();
        }
        Credentials credential = credentialsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credential not found with ID: " + id));

        ensureAccess(credential); // Check if current user can update

        if (request.getUserName() != null) credential.setUserName(request.getUserName());
        if (request.getMobileNumber() != null) credential.setMobileNumber(request.getMobileNumber());
        if (request.getPlatformName() != null) credential.setPlatformName(request.getPlatformName());
        Credentials updated = credentialsRepository.save(credential);

        return Response.<CredentialsResponse>builder()
                .data(toResponse(updated))
                .httpStatusCode(HttpStatus.OK.value())
                .message("Credential updated successfully")
                .build();
    }

    // Update credential
    @Transactional
    public Response<CredentialsResponse> updateDetails(Long id, CredentialsRequest request, String refId, String otp) {
        OtpEntry otpEntry = otpService.getOtpEntry(refId);

        if (otpEntry == null || otpEntry.isExpired()) {
            return Response.<CredentialsResponse>builder()
                    .data(null)
                    .httpStatusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("Invalid or expired OTP")
                    .build();
        }

        boolean valid = otpService.verifyOtp(refId, otp);
        if (!valid) {
            return Response.<CredentialsResponse>builder()
                    .data(null)
                    .httpStatusCode(HttpStatus.UNAUTHORIZED.value())
                    .message("Invalid OTP")
                    .build();
        }
        Credentials credential = credentialsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credential not found with ID: " + id));

        ensureAccess(credential); // Check if current user can update

        if (request.getDetails() != null) credential.setDetails(request.getDetails());

        Credentials updated = credentialsRepository.save(credential);

        return Response.<CredentialsResponse>builder()
                .data(toResponse(updated))
                .httpStatusCode(HttpStatus.OK.value())
                .message("Details updated successfully")
                .build();
    }

    // Toggle active/inactive
    public Response<CredentialsResponse> toggleActive(Long id) {
        Credentials credential = credentialsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credential not found with ID: " + id));

        ensureAccess(credential); // Check if current user can toggle
        Clients clients = credential.getClients();
        if(!clients.getActive()){
            throw new RuntimeException("Client is inactive: " + clients.getFirstName() + clients.getLastName());
        }
        if(credential.getActive()==false){
            if (jwtUtil.isAdminFromContext()){
                credential.setActive(true);
            }else{
                throw new RuntimeException("Access Denied to active credentials");
            }
        }else{
            credential.setActive(false);
        }
        Credentials updated = credentialsRepository.save(credential);

        String status = updated.getActive() ? "activated" : "deactivated";

        return Response.<CredentialsResponse>builder()
                .data(toResponse(updated))
                .httpStatusCode(HttpStatus.OK.value())
                .message("Credential " + status + " successfully")
                .build();
    }
    // Generate OTP for Update Details
    public Response<Map<String,Object>> generateOtpForUpdate(Long credentialId) {
        Credentials credential = credentialsRepository.findById(credentialId)
                .orElseThrow(() -> new RuntimeException("Credential not found with ID: " + credentialId));

        ensureAccess(credential); // Only admin/assigned user can generate OTP
        if(!credential.getActive()){
            throw new RuntimeException("Credential must be active!!!");
        }
        var otpResponse = otpService.generateOtpUsingId(credentialId);
        Map<String, Object> result = new HashMap<>();
        result.put("refId", otpResponse.getRefId());
        System.out.println("The OTP for the updating details is: "+otpResponse.getOtp());

        return Response.<Map<String,Object>>builder()
                .data(result)
                .httpStatusCode(HttpStatus.OK.value())
                .message("OTP generated successfully for credential ID: " + credentialId)
                .build();
    }
    // Generate OTP for Update Details
    public Response<Map<String,Object>> generateOtpForUpdateDetails(Long credentialId) {
        Credentials credential = credentialsRepository.findById(credentialId)
                .orElseThrow(() -> new RuntimeException("Credential not found with ID: " + credentialId));

        ensureAccess(credential); // Only admin/assigned user can generate OTP
        if(!credential.getActive()){
            throw new RuntimeException("Credential must be active!!!");
        }
        var otpResponse = otpService.generateOtpUsingId(credentialId);
        Map<String, Object> result = new HashMap<>();
        result.put("refId", otpResponse.getRefId());
        System.out.println("The OTP for the updating Details is: "+otpResponse.getOtp());

        return Response.<Map<String,Object>>builder()
                .data(result)
                .httpStatusCode(HttpStatus.OK.value())
                .message("OTP generated successfully for credential ID: " + credentialId)
                .build();
    }

    // Generate OTP for Details reveal
    public Response<Map<String,Object>> generateOtpForDetails(Long credentialId) {
        if(credentialId == null){
            throw new RuntimeException("Id must be shared for generating OTP!!!");
        }
        Credentials credential = credentialsRepository.findById(credentialId)
                .orElseThrow(() -> new RuntimeException("Credential not found with ID: " + credentialId));

        ensureAccess(credential); // Only admin/assigned user can generate OTP
        if(!credential.getActive()){
            throw new RuntimeException("Credential must be active!!!");
        }
        var otpResponse = otpService.generateOtpUsingId(credentialId);
        Map<String, Object> result = new HashMap<>();
        result.put("refId", otpResponse.getRefId());
        System.out.println("The OTP for the details is: "+otpResponse.getOtp());

        return Response.<Map<String,Object>>builder()
                .data(result)
                .httpStatusCode(HttpStatus.OK.value())
                .message("OTP generated successfully for credential ID: " + credentialId)
                .build();
    }

    // Reveal Details
    public Response<CredentialRevealResponse> revealDetails(String refId, String otp) {
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
        Long id = Long.parseLong(email);

        Credentials credentials = credentialsRepository.findById(id).orElse(null);


        ensureAccess(credentials); // Only admin/assigned user can reveal Details

        assert credentials != null;
        CredentialRevealResponse response = CredentialRevealResponse.builder()
                .credentialId(credentials.getId())
                .details(credentials.getDetails())
                .build();

        return Response.<CredentialRevealResponse>builder()
                .data(response)
                .httpStatusCode(HttpStatus.OK.value())
                .message("Details revealed successfully")
                .build();
    }

    public Response<List<String>> findPlatform(String keyword){
        jwtUtil.getAuthenticatedUserFromContext();
        String searchKeyword = (keyword != null) ? keyword : "";
        List<String> platforms = credentialsRepository.findDistinctPlatformName(searchKeyword);
        return Response.<List<String>>builder()
                .data(platforms)
                .httpStatusCode(200)
                .message("List of platform Name")
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



    // Mapper: Entity -> Response DTO (without Details)
    private CredentialsResponse toResponse(Credentials credential) {
        return CredentialsResponse.builder()
                .id(credential.getId())
                .clients(credential.getClients().getName())
                .userName(credential.getUserName())
                .mobileNumber(credential.getMobileNumber())
                .platformName(credential.getPlatformName())
                .active(credential.getActive())
                .createdAt(credential.getCreatedAt())
                .updatedAt(credential.getUpdatedAt())
                .build();
    }
}
*/
