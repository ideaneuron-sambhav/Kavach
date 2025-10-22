package com.login.Login.controller.auth;


import com.login.Login.dto.Response;
import com.login.Login.dto.otp.OtpVerifyRequest;
import com.login.Login.service.auth.AuthService;
import com.login.Login.dto.auth.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<Response<?>> loginUser(@Valid @RequestBody LoginRequest request) throws Exception{
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Response<?>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) throws Exception{
        return ResponseEntity.ok(authService.verifyOtp(request.getRefId(), request.getOtp()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Response<?>> logoutUser(HttpServletRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }


}
