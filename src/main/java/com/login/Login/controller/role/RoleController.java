package com.login.Login.controller.role;

import com.login.Login.dto.Response;
import com.login.Login.dto.role.RoleRequest;
import com.login.Login.service.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/role")
public class RoleController {
    @Autowired
    RoleService roleService;
    // List all roles
    @GetMapping("/list")
    public ResponseEntity<Response<?>> listRoles(@RequestParam(defaultValue = "") String search,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(roleService.list(search, page, size));
    }

    // Add a new role
    @PostMapping("/add")
    public ResponseEntity<Response<?>> addRole(@RequestBody RoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.registerRole(request));
    }

    // Delete a role
    @PutMapping("/active/{id}")
    public ResponseEntity<Response<?>> deleteRole(@PathVariable("id") Long roleId) {
        return ResponseEntity.ok(roleService.deleteRole(roleId));
    }

    // Update role name
    @PutMapping("/updaterolename")
    public ResponseEntity<Response<?>> updateRoleName(@RequestBody RoleRequest request) {
        return ResponseEntity.ok(roleService.updateRoleName(request));
    }

    // Update role permissions
    @PutMapping("/updaterolepermissions")
    public ResponseEntity<Response<?>> updateRolePermissions(@RequestBody RoleRequest request) {
        return ResponseEntity.ok(roleService.updateRolePermissions(request));
    }
}