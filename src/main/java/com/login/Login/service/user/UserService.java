package com.login.Login.service.user;

import com.login.Login.dto.Response;
import com.login.Login.dto.user.UserRequest;
import com.login.Login.dto.user.UserResponse;
import com.login.Login.entity.Role;
import com.login.Login.entity.User;
import com.login.Login.repository.RoleRepository;
import com.login.Login.repository.UserRepository;
import com.login.Login.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    @Autowired
    private final BCryptPasswordEncoder passwordEncoder;

    public Response<List<User>> listUsers() {
        jwtUtil.getAuthenticatedUserFromContext();

        List<User> users = userRepo.findAll();
        return Response.<List<User>>builder()
                .data(users)
                .httpStatusCode(200)
                .message("List of all users")
                .build();
    }


    @Transactional
    public Response<UserResponse> registerUser(UserRequest request) {
        try {

            jwtUtil.ensureAdminFromContext();
            User adminUser = jwtUtil.getAuthenticatedUserFromContext();

            // Validate required fields
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new RuntimeException("Email cannot be empty");
            }
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new RuntimeException("Password cannot be empty");
            }
            if (request.getFirstName() == null || request.getFirstName().isBlank()) {
                throw new RuntimeException("First name cannot be empty");
            }

            // Check if email already exists
            if (userRepo.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already registered");
            }

            // Encode password
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            Role role;


/*          Assigning role to admin if its null or blank
            String roleName = request.getRole()!= null ? request.getRole() : "user";
            Role role = roleRepository.findByNameIgnoreCase(roleName)
            .orElseThrow(()-> new RuntimeException("Role not found: "+ roleName));
*/

            String requestedRole = request.getRole();
            role = roleRepository.findByNameIgnoreCase(requestedRole)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + requestedRole));
            // Create user
            User user = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .role(role)
                    .active(true)
                    .build();

            User savedUser = userRepo.save(user);

            return Response.<UserResponse>builder()
                    .data(UserResponse.builder()
                            .id(user.getId())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .role(user.getRole().getName())
                            .permissionIds(user.getPermissions())
                            .active(user.getActive())
                            .build())
                    .httpStatusCode(200)
                    .message("User Created Successfully by admin: " + adminUser.getEmail())
                    .build();


        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Database constraint violation: " + e.getMostSpecificCause().getMessage());
        } catch (RuntimeException e) {
            throw e; // Pass RuntimeExceptions like "Email cannot be empty"
        } catch (Exception e) {
            throw new RuntimeException("Error registering user: " + e.getMessage());
        }
    }
    @Transactional
    public Response<UserResponse> toggleUser(Long userId) {
        jwtUtil.ensureAdminFromContext();
        User adminUser = jwtUtil.getAuthenticatedUserFromContext();


        // Fetch user by ID or email
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Toggle active
        boolean currentStatus = user.getActive();
        // Prevent toggling admin user
        if ("admin".equalsIgnoreCase(user.getRole().getName()) && currentStatus) {
            long activeAdminCount = userRepo.findAll().stream()
                    .filter(u -> u.getRole() != null)
                    .filter(u -> "admin".equalsIgnoreCase(u.getRole().getName()))
                    .filter(User::getActive) // only active admins
                    .count();

            if (activeAdminCount <= 1) {
                throw new RuntimeException("Cannot deactivate the last active admin user");
            }

        }

        user.setActive(!currentStatus);
        userRepo.save(user);

        return Response.<UserResponse>builder()
                .data(UserResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .role(user.getRole().getName())
                        .permissionIds(user.getPermissions())
                        .active(user.getActive())
                        .build())
                .httpStatusCode(200)
                .message((user.getActive() ? "User activated" : "User deactivated") + " by admin: " + adminUser.getEmail())
                .build();
    }


}
