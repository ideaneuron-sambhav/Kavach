package com.login.Login.controller.file;

import com.login.Login.dto.Response;
import com.login.Login.entity.User;
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
@RequestMapping("/files/")
public class FileController {
    @Autowired
    FileService fileService;
    @Autowired
    JwtUtil jwtUtil;

    @PostMapping({"/**","/"})
    @Transactional
    public ResponseEntity<Response<?>> uploadFile(HttpServletRequest request, @RequestParam("file") List<MultipartFile> files) throws RuntimeException, IOException {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        String path = user.getId() + "/" + request.getRequestURI().substring("/files/".length());
        return ResponseEntity.ok(fileService.uploadFile(path, files));
    }

    @GetMapping(value = {"/**","/"})
    public ResponseEntity<?> downloadFile(HttpServletRequest request) throws RuntimeException, IOException {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        String path = user.getId() + "/" + request.getRequestURI().substring("/files/".length());
        return fileService.downloadFile(path);
    }
}
