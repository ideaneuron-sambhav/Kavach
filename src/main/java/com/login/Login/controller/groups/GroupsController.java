package com.login.Login.controller.groups;

import com.login.Login.dto.Response;
import com.login.Login.dto.groups.GroupsRequest;
import com.login.Login.dto.groups.GroupsResponse;
import com.login.Login.service.groups.GroupsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupsController {
    @Autowired
    GroupsService groupsService;


    @PostMapping("/add")
    public ResponseEntity<Response<GroupsResponse>> addGroups(@RequestBody GroupsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupsService.registerGroups(request));
    }

    @GetMapping("/list")
    public ResponseEntity<Response<Page<GroupsResponse>>> listGroups(@RequestParam(defaultValue = "") String search,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(groupsService.listGroups(search, page, size));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response<GroupsResponse>> updateGroups(@PathVariable Long id,
                                                                 @RequestBody GroupsRequest request) {
        return ResponseEntity.ok(groupsService.updateGroups(id, request));
    }

    @PutMapping("/toggle/{id}")
    public ResponseEntity<Response<GroupsResponse>> toggleActiveStatus(@PathVariable Long id) {
        return ResponseEntity.ok(groupsService.toggleActiveStatus(id));
    }
}