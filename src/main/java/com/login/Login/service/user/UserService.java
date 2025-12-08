package com.login.Login.service.user;

import com.login.Login.dto.Response;
import com.login.Login.dto.user.UserRequest;
import com.login.Login.dto.user.UserResponse;
import com.login.Login.entity.Folder;
import com.login.Login.entity.Role;
import com.login.Login.entity.User;
import com.login.Login.exception.InvalidCredentialsException;
import com.login.Login.repository.RoleRepository;
import com.login.Login.repository.UserRepository;
import com.login.Login.security.JwtUtil;
import com.login.Login.service.email.EmailService;
import com.login.Login.service.email.JwtPasswordService;
import com.login.Login.service.folder.FolderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;


@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    UserRepository userRepo;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    FolderService folderService;
    @Autowired
    BCryptPasswordEncoder passwordEncoder;
    @Autowired
    EmailService emailService;
    @Autowired
    JwtPasswordService jwtPasswordService;

    private static final String url = "http://kavach.com";

    public Response<Page<UserResponse>> listUsers(String keyword, int page, int size) {
        jwtUtil.ensureAdminFromContext();
        Role role = roleRepository.findByNameIgnoreCase("Clients").orElseThrow(()-> new RuntimeException("Error"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").descending());
        Page<User> usersPage;
        if (keyword != null && !keyword.isBlank()) {
            usersPage = userRepo.searchUsers(keyword.trim(), pageable, role);
        } else {
            usersPage = userRepo.findAllByRoleNot(pageable, role);
        }
        Page<UserResponse> userResponses = usersPage.map(user -> UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .active(user.getActive())
                .permissionIds(user.getPermissions())
                .build()
        );

        return Response.<Page<UserResponse>>builder()
                .data(userResponses)
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
            /*if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new RuntimeException("Password cannot be empty");
            }*/
            if (request.getFirstName() == null || request.getFirstName().isBlank()) {
                throw new RuntimeException("First name cannot be empty");
            }

            // Check if email already exists
            if (userRepo.findByEmail(request.getEmail().toLowerCase()).isPresent()) {
                throw new RuntimeException("Email already registered");
            }

            Role role;


/*          Assigning role to admin if its null or blank
            String roleName = request.getRole()!= null ? request.getRole() : "user";
            Role role = roleRepository.findByNameIgnoreCase(roleName)
            .orElseThrow(()-> new RuntimeException("Role not found: "+ roleName));
*/
            String password = generatePassword(request.getFirstName());
            String encodedPassword = passwordEncoder.encode(password);
            //emailService.sendPasswordEmail(request.getEmail(), password);

            String requestedRole = request.getRole();
            role = roleRepository.findByNameIgnoreCase(requestedRole)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + requestedRole));
            // Create user
            User user = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail().toLowerCase())
                    .password(encodedPassword)
                    .role(role)
                    .hashPIN(null)
                    .active(true)
                    .build();

            userRepo.save(user);
            Folder folder = folderService.createUserRootFolder(user.getId());
            user.setRootFolder(folder);
            userRepo.save(user);
            String token = jwtPasswordService.generatePasswordResetToken(user.getEmail());
            String resetLink = url + "/reset-password?token=" + token;
            emailService.sendRegistrationEmail(user.getEmail(), resetLink);
            return Response.<UserResponse>builder()
                    .data(UserResponse.builder()
                            .id(user.getId())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail().toLowerCase())
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

    public Response<Object> sendPasswordResetEmail(String email) {
        String token = jwtPasswordService.generatePasswordResetToken(email);
        String resetLink = url + "/reset-password?token=" + token;
        emailService.sendPasswordLinkEmail(email,resetLink);
        return Response.builder().data(null).httpStatusCode(200).message("Password reset link sent to email.").build();
    }

    @Transactional
    public Response<Object> changePassword(UserRequest request) {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Wrong Password!");
        }
        if(request.getPassword().equals(request.getConfirmPassword())){
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }else{
            throw new RuntimeException("Password mismatched");
        }
        return Response.builder().data(null).httpStatusCode(200).message("PIN updated successfully!").build();
    }

    @Transactional
    public Response<Object> updateHashPIN(String PIN) {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        if (PIN == null || !PIN.matches("\\d{6}")) {
            throw new RuntimeException("PIN must be 6 digits and numeric.");
        }
        user.setHashPIN(passwordEncoder.encode(PIN));
        return Response.builder().data(null).httpStatusCode(200).message("PIN updated successfully!").build();
    }

    @Transactional
    public Response<Object> updatePassword(String email, String newPassword) {
        User user =userRepo.findByEmail(email).orElseThrow(()-> new RuntimeException("User Not Found!"));
        user.setPassword(passwordEncoder.encode(newPassword));
        return Response.builder().data(null).httpStatusCode(200).message("Password updated successfully!").build();
    }

    public Response<?> updateUserRole(Long id, String roleName){
        jwtUtil.ensureAdminFromContext();
        User user = userRepo.findById(id).orElseThrow(()-> new RuntimeException("User Not Found"));
        Role role = roleRepository.findByNameIgnoreCase(roleName).orElseThrow(()-> new RuntimeException("Role Not Found"));
        user.setRole(role);
        userRepo.save(user);
        return Response.builder().httpStatusCode(201).message("Role Updated Successfully!").build();
    }

    public Response<?> toggleActive(Long id){
        jwtUtil.ensureAdminFromContext();
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        user.setActive(!user.getActive());
        User updated = userRepo.save(user);

        String status = updated.getActive() ? "activated" : "deactivated";

        return Response.builder()
                .data(null)
                .httpStatusCode(HttpStatus.OK.value())
                .message("User " + status + " successfully")
                .build();
    }

    public Response<UserResponse> updateUser(UserRequest request){
        User user = jwtUtil.getAuthenticatedUserFromContext();
        if (request.getFirstName() != null && !request.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().equals(user.getLastName())) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepo.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        User user1 = userRepo.save(user);
        return Response.<UserResponse>builder()
                .data(UserResponse.builder()
                        .id(user1.getId())
                        .firstName(user1.getFirstName())
                        .lastName(user1.getLastName())
                        .email(user1.getEmail().toLowerCase())
                        .role(user1.getRole().getName())
                        .permissionIds(user1.getPermissions())
                        .active(user1.getActive())
                        .build())
                .httpStatusCode(200)
                .message("User Updated Successfully")
                .build();

    }


    private String generatePassword(String name) {

        String clean = name.trim().toUpperCase();

        // First four letters (pad with x if less than 4)
        String firstFour = clean.length() >= 4 ? clean.substring(0, 4) : String.format("%-4s", clean).replace(' ', 'X');

        // Generate 4 random digits
        SecureRandom random = new SecureRandom();
        int digits = 1000 + random.nextInt(9000);

        return firstFour + digits;
    }

}
