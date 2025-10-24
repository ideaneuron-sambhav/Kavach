package com.login.Login.service.auth;

import com.login.Login.dto.Response;
import com.login.Login.dto.auth.AuthResponse;
import com.login.Login.dto.auth.LoginRequest;
import com.login.Login.entity.Permission;
import com.login.Login.entity.User;
import com.login.Login.exception.InvalidCredentialsException;
import com.login.Login.exception.UserNotFoundException;
import com.login.Login.service.otp.OtpEntry;
import com.login.Login.dto.otp.OtpResponse;
import com.login.Login.service.otp.OtpService;
import com.login.Login.security.JwtBlacklistService;
import com.login.Login.security.JwtUtil;
import com.login.Login.repository.PermissionRepository;
import com.login.Login.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    private final JwtUtil jwtUtil;

    @Autowired
    private final JwtBlacklistService jwtBlacklistService;

    private final PermissionRepository permissionRepo;

    @Autowired
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private final OtpService otpService;


    public Response<String> login(LoginRequest request) throws Exception {

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Wrong Password!");
        }

        if (!user.getActive()) {
            throw new InvalidCredentialsException("User is inactive!");
        }

        // Generate OTP using refId
        OtpResponse otpResponse = otpService.generateOtp(user.getEmail());



        // optional for debugging
        System.out.println("Generated OTP for " + user.getEmail() + ": " + otpResponse.getOtp());
        System.out.println("RefId: " + otpResponse.getRefId());

        return Response.<String>builder()
                .data(otpResponse.getRefId())
                .httpStatusCode(200)
                .message("OTP generated successfully.")
                .build();
    }

    // --------------------- VERIFY OTP ---------------------
    public Response<AuthResponse> verifyOtp(String refId, String otp) throws Exception {
        OtpEntry entry = otpService.getOtpEntry(refId);
        if (entry == null) {
            throw new InvalidCredentialsException("Invalid or expired OTP");
        }


        User user = userRepo.findByEmail(entry.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getActive()) {
            throw new InvalidCredentialsException("User is inactive!");
        }

        boolean valid = otpService.verifyOtp(refId, otp);
        if (!valid) {
            int remaining = otpService.remainingAttempts(refId);
            throw new InvalidCredentialsException("Invalid or expired OTP. Remaining attempts: " + remaining);
        }

        // Expire previous JWT token
        String previousToken = jwtBlacklistService.getActiveToken(user.getEmail());
        if (previousToken != null) {
            jwtBlacklistService.blacklistToken(
                    previousToken,
                    System.currentTimeMillis() + jwtUtil.getExpirationMillisSafe(previousToken),
                    "Token Logged Out due to Another Login"
            );
        }


        // Generate new JWT
        String token = jwtUtil.generateToken(user.getEmail());
        jwtBlacklistService.setActiveToken(user.getEmail(), token);

        // Prepare permissions
        List<Long> permissionIds = user.getRole().getPermissionIds() != null
                ? user.getRole().getPermissionIds()
                : List.of();

        List<String> permissionStrings = permissionRepo.findAllById(permissionIds)
                .stream()
                .map(Permission::getPermissionType)
                .collect(Collectors.toList());

        AuthResponse authResponse = new AuthResponse(
                token,
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().getName(),
                permissionStrings,
                permissionIds
        );

        return Response.<AuthResponse>builder()
                .data(authResponse)
                .httpStatusCode(200)
                .message("Login successful")
                .build();
    }

    // --------------------- LOGOUT ---------------------
    public Response<String> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return Response.<String>builder()
                    .data(null)
                    .httpStatusCode(400)
                    .message("No token provided")
                    .build();
        }

        String token = header.substring(7);

        // Validate and blacklist token
        jwtUtil.validateToken(token); // throws if expired/invalid
        long expiry = jwtUtil.getExpirationMillisSafe(token);
        jwtBlacklistService.logoutToken(token, expiry);

        return Response.<String>builder()
                .data("User logged out successfully")
                .httpStatusCode(200)
                .message("Logout successful")
                .build();
    }
}
