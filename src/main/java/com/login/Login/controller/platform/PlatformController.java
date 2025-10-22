package com.login.Login.controller.platform;

import com.login.Login.dto.Response;
import com.login.Login.dto.platform.*;
import com.login.Login.service.platform.PlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/platforms")
@RequiredArgsConstructor
public class PlatformController {

    @Autowired
    PlatformService platformService;

    // Add platform (admin only)
    @PostMapping("/add")
    public ResponseEntity<Response<PlatformResponse>> addPlatform(
            @RequestBody PlatformRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(platformService.addPlatform(request));
    }

    // Update platform (admin only)
    @PutMapping("/update/{id}")
    public ResponseEntity<Response<PlatformResponse>> updatePlatform(
            @PathVariable Long id,
            @RequestBody PlatformRequest request) {
        return ResponseEntity.ok(platformService.updatePlatform(id, request));
    }

    // Toggle active/inactive (admin only)
    @PutMapping("/toggle/{id}")
    public ResponseEntity<Response<PlatformResponse>> toggleActiveStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(platformService.toggleActiveStatus(id));
    }

    // List all platforms (admin only)
    @GetMapping("/list")
    public ResponseEntity<Response<List<PlatformResponse>>> listPlatforms() {
        return ResponseEntity.ok(platformService.listPlatforms());
    }
}
