package com.login.Login.service.platform;

import com.login.Login.dto.Response;
import com.login.Login.dto.platform.*;
import com.login.Login.entity.Platform;
import com.login.Login.repository.PlatformRepository;
import com.login.Login.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformService {
    private final JwtUtil jwtUtil;
    private final PlatformRepository platformRepository;

    // Add new platform
    @Transactional
    public Response<PlatformResponse> addPlatform(PlatformRequest request) {
        jwtUtil.ensureAdminFromContext();
        if (platformRepository.existsByName(request.getName())) {
            return Response.<PlatformResponse>builder()
                    .data(null)
                    .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                    .message("Platform name already exists")
                    .build();
        }
        Platform platform = Platform.builder()
                .name(request.getName())
                .twoFA(request.getTwoFA())
                .twoFATypes(request.getTwoFATypes())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        if (Boolean.TRUE.equals(request.getTwoFA())) {
            if (request.getTwoFATypes() == null || request.getTwoFATypes().isEmpty()) {
                throw new IllegalArgumentException("twoFATypes must be provided when twoFA is true");
            }

            // ensure only allowed values are present
            for (String type : request.getTwoFATypes()) {
                if (!type.equalsIgnoreCase("SMS") && !type.equalsIgnoreCase("Email")) {
                    throw new IllegalArgumentException("twoFATypes can only contain: SMS or Email");
                }
            }
        } else {
            // if twoFA is false, ignore or clear types
            if (request.getTwoFATypes() != null && !request.getTwoFATypes().isEmpty()) {
                throw new IllegalArgumentException("twoFATypes must be empty when twoFA is false");
            }
        }


        Platform saved = platformRepository.save(platform);

        return Response.<PlatformResponse>builder()
                .data(toResponse(saved))
                .httpStatusCode(HttpStatus.CREATED.value())
                .message("Platform created successfully")
                .build();
    }


    // Update platform details
    @Transactional
    public Response<PlatformResponse> updatePlatform(Long id, PlatformRequest request) {
        jwtUtil.ensureAdminFromContext();
        Platform platform = platformRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Platform not found with ID: " + id));

        if (request.getName() != null) platform.setName(request.getName());
        if (request.getTwoFA() != null) platform.setTwoFA(request.getTwoFA());
        if (request.getTwoFATypes() != null) platform.setTwoFATypes(request.getTwoFATypes());
        if (request.getActive() != null) platform.setActive(request.getActive());

        Platform updated = platformRepository.save(platform);

        return Response.<PlatformResponse>builder()
                .data(toResponse(updated))
                .httpStatusCode(HttpStatus.OK.value())
                .message("Platform updated successfully")
                .build();
    }

    // Toggle active/inactive
    @Transactional
    public Response<PlatformResponse> toggleActiveStatus(Long id) {
        jwtUtil.ensureAdminFromContext();
        Platform platform = platformRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Platform not found with ID: " + id));

        platform.setActive(!platform.getActive());
        Platform updated = platformRepository.save(platform);

        String status = updated.getActive() ? "activated" : "deactivated";

        return Response.<PlatformResponse>builder()
                .data(toResponse(updated))
                .httpStatusCode(HttpStatus.OK.value())
                .message("Platform " + status + " successfully")
                .build();
    }

    // List all platforms
    public Response<List<PlatformResponse>> listPlatforms() {
        jwtUtil.ensureAdminFromContext();
        List<PlatformResponse> platforms = platformRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return Response.<List<PlatformResponse>>builder()
                .data(platforms)
                .httpStatusCode(HttpStatus.OK.value())
                .message("Platform list fetched successfully")
                .build();
    }

    // Convert entity to response DTO
    private PlatformResponse toResponse(Platform platform) {
        return PlatformResponse.builder()
                .id(platform.getId())
                .name(platform.getName())
                .twoFA(platform.getTwoFA())
                .twoFATypes(platform.getTwoFATypes())
                .active(platform.getActive())
                .createdAt(platform.getCreatedAt())
                .updatedAt(platform.getUpdatedAt())
                .build();
    }
}
