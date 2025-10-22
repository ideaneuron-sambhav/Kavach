package com.login.Login.service.permission;

import com.login.Login.dto.Response;
import com.login.Login.dto.permission.PermissionRequest;
import com.login.Login.dto.permission.PermissionResponse;
import com.login.Login.entity.Permission;
import com.login.Login.entity.Role;
import com.login.Login.entity.User;
import com.login.Login.repository.RoleRepository;
import com.login.Login.security.JwtUtil;
import com.login.Login.repository.PermissionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PermissionService {

    private final JwtUtil jwtUtil;
    private final PermissionRepository permissionRepo;
    private final RoleRepository roleRepository;


    // List all permissions
    public Response<List<Permission>> list() {
        jwtUtil.getAuthenticatedUserFromContext();
        List<Permission> permissions = permissionRepo.findAll();


        return Response.<List<Permission>>builder()
                .data(permissions)
                .httpStatusCode(200)
                .message("List of all permissions")
                .build();
    }

    // List permissions by role
    public Response<List<Permission>> listByRole(String roleName) {
        jwtUtil.getAuthenticatedUserFromContext();

        roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        List<Permission> permissionsByRoleName = permissionRepo.findPermissionsByRoleName(roleName);

        return Response.<List<Permission>>builder()
                .data(permissionsByRoleName)
                .httpStatusCode(200)
                .message("List of all permissions registered with the role: " + roleName)
                .build();
    }

    // Register a new permission (Admin only)
    @Transactional
    public Response<PermissionResponse> registerPermission(PermissionRequest request) {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        jwtUtil.ensureAdminFromContext();

        if (request.getPermissionType() == null || request.getPermissionType().isBlank()) {
            throw new RuntimeException("Permission cannot be empty");
        }

        if (permissionRepo.findByPermissionType(request.getPermissionType()).isPresent()) {
            throw new RuntimeException("Permission already exists: " + request.getPermissionType());
        }

        Permission permission = Permission.builder()
                .permissionType(request.getPermissionType())
                .active(true)
                .build();

        Permission savedPermission = permissionRepo.save(permission);
        // Automatically assign this permission to the admin role
        Role adminRole = roleRepository.findByNameIgnoreCase("admin")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        List<Long> updatedPermissions = adminRole.getPermissionIds() != null
                ? adminRole.getPermissionIds()
                : new ArrayList<>();

        updatedPermissions.add(savedPermission.getId());
        adminRole.setPermissionIds(updatedPermissions);
        roleRepository.save(adminRole);

        PermissionResponse response = PermissionResponse.builder()
                .id(savedPermission.getId())
                .permissionType(savedPermission.getPermissionType())
                .active(savedPermission.getActive())
                .build();

        return Response.<PermissionResponse>builder()
                .data(response)
                .httpStatusCode(201)
                .message("Permission added successfully by admin: " + user.getEmail())
                .build();
    }

    // Update permission (Admin only)
    @Transactional
    public Response<PermissionResponse> updatePermission(PermissionRequest request) {
        jwtUtil.ensureAdminFromContext();
        User user = jwtUtil.getAuthenticatedUserFromContext();

        if (request.getOldPermissionType() == null || request.getOldPermissionType().isBlank()) {
            throw new RuntimeException("Old permission type cannot be empty");
        }

        if (request.getPermissionType() == null || request.getPermissionType().isBlank()) {
            throw new RuntimeException("New permission type cannot be empty");
        }

        Permission permission = permissionRepo.findByPermissionType(request.getOldPermissionType())
                .orElseThrow(() -> new RuntimeException("Old permission not found: " + request.getOldPermissionType()));

        if (permissionRepo.findByPermissionType(request.getPermissionType()).isPresent()) {
            throw new RuntimeException("New permission already exists: " + request.getPermissionType());
        }

        permission.setPermissionType(request.getPermissionType());
        Permission updatedPermission = permissionRepo.save(permission);

        PermissionResponse response = PermissionResponse.builder()
                .id(updatedPermission.getId())
                .permissionType(updatedPermission.getPermissionType())
                .active(updatedPermission.getActive())
                .build();

        return Response.<PermissionResponse>builder()
                .data(response)
                .httpStatusCode(200)
                .message("Permission updated successfully by admin: " + user.getEmail())
                .build();
    }

    // Deactivate permission (Admin only)
    @Transactional
    public Response<PermissionResponse> togglePermission(Long permissionId) {
        jwtUtil.ensureAdminFromContext();
        User user = jwtUtil.getAuthenticatedUserFromContext();

        Permission permission = permissionRepo.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with ID: " + permissionId));

        boolean currentStatus = permission.getActive();
        if(currentStatus){
            // Mark inactive instead of deleting
            permission.setActive(false);
            permissionRepo.save(permission);

            // Remove this permission from all roles
            List<Role> allRoles = roleRepository.findAll();
            for (Role role : allRoles) {
                if (role.getPermissionIds() != null && role.getPermissionIds().contains(permission.getId())) {
                    role.getPermissionIds().remove(permission.getId());
                    roleRepository.save(role);
                }
            }
            return Response.<PermissionResponse>builder()
                    .data(PermissionResponse.builder()
                            .id(permission.getId())
                            .permissionType(permission.getPermissionType())
                            .active(permission.getActive())
                            .build())
                    .httpStatusCode(200)
                    .message("Permission deactivated successfully by admin: " + user.getEmail())
                    .build();
        }else{
            permission.setActive(true);
            permissionRepo.save(permission);
            Role adminRole = roleRepository.findByNameIgnoreCase("admin")
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            List<Long> permissionIds = adminRole.getPermissionIds() != null
                    ? adminRole.getPermissionIds()
                    : new ArrayList<>();

            if (!permissionIds.contains(permission.getId())) {
                permissionIds.add(permission.getId());
                adminRole.setPermissionIds(permissionIds);
                roleRepository.save(adminRole);
            }

            return Response.<PermissionResponse>builder()
                    .data(PermissionResponse.builder()
                            .id(permission.getId())
                            .permissionType(permission.getPermissionType())
                            .active(permission.getActive())
                            .build())
                    .httpStatusCode(200)
                    .message("Permission reactivated successfully by admin: " + user.getEmail())
                    .build();
        }
    }
}