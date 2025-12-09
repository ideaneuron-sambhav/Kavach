package com.login.Login.controller.auth;


import com.login.Login.dto.Response;
import com.login.Login.dto.otp.OtpVerifyRequest;
import com.login.Login.dto.user.UserRequest;
import com.login.Login.service.email.JwtPasswordService;
import com.login.Login.service.auth.AuthService;
import com.login.Login.dto.auth.LoginRequest;
import com.login.Login.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    JwtPasswordService jwtPasswordService;

    @Autowired
    UserService userService;


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

    @PostMapping("/set-pin")        //Using JWT LOGIN TOKEN
    public ResponseEntity<Response<?>> setHashPIN(@RequestParam String PIN) {
        return ResponseEntity.ok(userService.updateHashPIN(PIN));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Response<?>> changePassword(@RequestBody UserRequest request){
        return ResponseEntity.ok(userService.changePassword(request));
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<Response<?>> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(userService.sendPasswordResetEmail(email));
    }

    @GetMapping("/reset-password")
    public ResponseEntity<Response<?>> resetPasswordPage(@RequestParam String token) {
        if (!jwtPasswordService.validatePasswordResetToken(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Response.builder().data(null).httpStatusCode(HttpStatus.BAD_REQUEST.value()).message("Invalid or expired token!").build());
        }
        String email = jwtPasswordService.getEmailFromResetToken(token);
        return ResponseEntity.ok(Response.builder().httpStatusCode(200).message("Valid token. You can now set a new password for " + email).build());
    }

    @PostMapping("/set-password")
    public ResponseEntity<Response<?>> setPassword(@RequestParam String token, @RequestBody UserRequest request) {
        if (!jwtPasswordService.validatePasswordResetToken(token)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Response.builder().data(null).httpStatusCode(HttpStatus.BAD_REQUEST.value()).message("Invalid or expired token!").build());
        }
        String email = jwtPasswordService.getEmailFromResetToken(token);
        return ResponseEntity.ok(userService.updatePassword(email, request.getPassword(), request.getConfirmPassword()));
    }


}
