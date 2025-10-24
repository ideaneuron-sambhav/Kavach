package com.login.Login.controller.user;

import com.login.Login.dto.Response;
import com.login.Login.dto.user.UserRequest;
import com.login.Login.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Response<?>> registerUser(@RequestBody UserRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
    }
    @PutMapping("/active/{id}")
    public ResponseEntity<Response<?>> toggleUser(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(userService.toggleUser(userId));
    }

    // List all users with active status
    @GetMapping("/list")
    public ResponseEntity<Response<?>> listUsers(@RequestParam(defaultValue = "") String search,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.listUsers(search, page, size));

    }
}
