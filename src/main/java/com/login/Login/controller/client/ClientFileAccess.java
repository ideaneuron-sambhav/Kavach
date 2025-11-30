package com.login.Login.controller.client;

import com.login.Login.dto.Response;
import com.login.Login.dto.clients.ClientIdRequest;
import com.login.Login.entity.Clients;
import com.login.Login.entity.User;
import com.login.Login.repository.ClientRepository;
import com.login.Login.security.JwtUtil;
import com.login.Login.service.file.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/clients/files/")
public class ClientFileAccess {
    @Autowired
    FileService fileService;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    ClientRepository clientRepository;

    @PostMapping({"/**","/"})
    @Transactional
    public ResponseEntity<Response<?>> uploadFile(HttpServletRequest request, @RequestBody ClientIdRequest clientRequest, @RequestParam("file") List<MultipartFile> files, @RequestBody Long ClientId) throws RuntimeException, IOException {
        jwtUtil.ensureAdminFromContext();
        Clients client = clientRepository.findById(clientRequest.getClientId()).orElseThrow(()-> new RuntimeException("Client Not Found!!!"));
        String path = client.getUserId().getId() + "/" + request.getRequestURI().substring("/clients/files/".length());
        return ResponseEntity.ok(fileService.uploadFile(path, files));
    }

    @GetMapping(value = {"/**","/"})
    public ResponseEntity<?> downloadFile(HttpServletRequest request, @RequestBody ClientIdRequest clientRequest) throws RuntimeException, IOException {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        boolean isAdmin = jwtUtil.isAdminFromContext();
        Clients client = clientRepository.findById(clientRequest.getClientId()).orElseThrow(()-> new RuntimeException("Client Not Found!!!"));
        String path;
        if(isAdmin){
            path = client.getUserId().getId() + "/" + request.getRequestURI().substring("/clients/files/".length());
        }else if(client.getAssignedUser() == user){
            path = client.getUserId().getId() + "/" + request.getRequestURI().substring("/clients/files/".length());
        }else{
            throw new RuntimeException("Access Denied: Client not assigned to user");
        }
        return fileService.downloadFile(path);
    }
}