package com.login.Login.service.role;


import com.login.Login.dto.Response;
import com.login.Login.dto.role.RoleRequest;
import com.login.Login.dto.role.RoleResponse;
import com.login.Login.entity.Permission;
import com.login.Login.entity.Role;
import com.login.Login.entity.User;
import com.login.Login.repository.UserRepository;
import com.login.Login.security.JwtUtil;
import com.login.Login.repository.PermissionRepository;
import com.login.Login.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public Response<Page<Role>> list(String keyword, int page, int size) {
            jwtUtil.getAuthenticatedUserFromContext();
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            Page<Role> rolesPage;
            if(keyword != null && !keyword.isBlank()){
                rolesPage = roleRepository.searchRoles(keyword.trim(), pageable);
            }else{
                rolesPage = roleRepository.findAll(pageable);
            }

            return Response.<Page<Role>>builder()
                    .data(rolesPage)
                    .httpStatusCode(200)
                    .message("List of all roles")
                    .build();

    }
    // Register role
    public Response<RoleResponse> registerRole(RoleRequest request) {
        jwtUtil.ensureAdminFromContext();
        User user = jwtUtil.getAuthenticatedUserFromContext();

        if (request.getRoleName() == null || request.getRoleName().isBlank())
            throw new RuntimeException("Role cannot be empty");

        if (roleRepository.findByNameIgnoreCase(request.getRoleName()).isPresent())
            throw new RuntimeException("Role name already exists: " + request.getRoleName());

        Role saved = roleRepository.save(Role.builder().name(request.getRoleName()).build());

        RoleResponse response = RoleResponse.builder()
                .id(saved.getId())
                .roleName(saved.getName())
                .active(saved.getActive())
                .build();

        return Response.<RoleResponse>builder()
                .data(response)
                .httpStatusCode(201)
                .message("Role created successfully by admin: " + user.getEmail())
                .build();
    }

    // Delete role
    public Response<RoleResponse> deleteRole(Long roleId) {
        jwtUtil.ensureAdminFromContext();
        User user = jwtUtil.getAuthenticatedUserFromContext();

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

        if ("admin".equalsIgnoreCase(role.getName())) {
            throw new RuntimeException("Admin role cannot be deactivated");
        }


        boolean currentStatus = role.getActive();
        if (currentStatus) {
            // Deactivate role
            role.setActive(false);
            roleRepository.save(role);

            List<User> users = userRepository.findAllByRole(role); // create this query in UserRepository
            for (User u : users) {
                u.setActive(false);
                userRepository.save(u);
            }
            return Response.<RoleResponse>builder()
                    .data(RoleResponse.builder()
                            .id(role.getId())
                            .roleName(role.getName())
                            .permissionIds(role.getPermissionIds())
                            .active(role.getActive())
                            .build())
                    .httpStatusCode(200)
                    .message("Role deactivated successfully by admin: " + user.getEmail())
                    .build();

        } else {
            // Reactivate role
            role.setActive(true);
            roleRepository.save(role);

            // Optional: assign back permissions to admin only if needed

            return Response.<RoleResponse>builder()
                    .data(RoleResponse.builder()
                            .id(role.getId())
                            .roleName(role.getName())
                            .permissionIds(role.getPermissionIds())
                            .active(role.getActive())
                            .build())
                    .httpStatusCode(200)
                    .message("Role reactivated successfully by admin: " + user.getEmail())
                    .build();
        }
    }

    // Update role name
    public Response<RoleResponse> updateRoleName(RoleRequest request) {
        jwtUtil.ensureAdminFromContext();
        User user = jwtUtil.getAuthenticatedUserFromContext();

        Role role = roleRepository.findByNameIgnoreCase(request.getOldRoleName())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getOldRoleName()));

        role.setName(request.getRoleName());
        Role updated = roleRepository.save(role);

        RoleResponse response = RoleResponse.builder()
                .id(updated.getId())
                .roleName(updated.getName())
                .permissionIds(updated.getPermissionIds())
                .active(updated.getActive())
                .build();

        return Response.<RoleResponse>builder()
                .data(response)
                .httpStatusCode(200)
                .message("Role name updated successfully by admin: " + user.getEmail())
                .build();
    }

    // Update role permissions
    public Response<RoleResponse> updateRolePermissions(RoleRequest request) {
        jwtUtil.ensureAdminFromContext();
        User user = jwtUtil.getAuthenticatedUserFromContext();

        Role role = roleRepository.findByNameIgnoreCase(request.getRoleName())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleName()));
        if ("admin".equalsIgnoreCase(request.getRoleName())) {
            throw new RuntimeException("Admin role permissions cannot be updated");
        }

        List<Permission> permissions = permissionRepository.findAllByIdInAndActiveTrue(request.getPermissionIds());
        if (permissions.isEmpty())
            throw new RuntimeException("No valid permissions found for given IDs");



        role.setPermissionIds(permissions.stream().map(Permission::getId).collect(Collectors.toList()));
        Role updated = roleRepository.save(role);

        RoleResponse response = RoleResponse.builder()
                .id(updated.getId())
                .roleName(updated.getName())
                .permissionIds(updated.getPermissionIds())
                .active(updated.getActive())
                .build();

        return Response.<RoleResponse>builder()
                .data(response)
                .httpStatusCode(200)
                .message("Role permissions updated successfully by admin: " + user.getEmail())
                .build();
    }
}