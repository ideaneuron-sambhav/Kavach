package com.login.Login.controller.permission;

import com.login.Login.dto.Response;
import com.login.Login.dto.permission.PermissionRequest;
import com.login.Login.entity.Permission;
import com.login.Login.service.permission.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permission")
public class PermissionController {
    @Autowired
    PermissionService permissionService;

    @PostMapping("/add")
    public ResponseEntity<Response<?>> addPermission(@RequestBody PermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.registerPermission(request));
    }

    // toggle permission
    @PutMapping("/active/{id}")
    public Response<?> deletePermission(@PathVariable("id") Long permissionId) {

        return permissionService.togglePermission(permissionId);
    }

    // Update permission
    @PutMapping("/update")
    public ResponseEntity<Response<?>> updatePermission(@RequestBody PermissionRequest request) {
        return ResponseEntity.ok(permissionService.updatePermission(request));
    }

    // List all permissions
    @GetMapping("/list")
    public Response<Page<Permission>> listPermissions(@RequestParam(defaultValue = "") String search,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return permissionService.list(search, page, size);
    }

    // List permissions by role (case-insensitive)
    @GetMapping("/role/{roleName}")
    public Response<?> listPermissionsByRole(@PathVariable String roleName) {
        return permissionService.listByRole(roleName.toLowerCase());
    }
}