package com.login.Login.controller;

import com.login.Login.entity.Folder;
import com.login.Login.entity.User;
import com.login.Login.repository.FolderRepository;
import com.login.Login.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    FolderRepository folderRepository;

    @PostMapping({"/**","/"})
    @Transactional
    public Map<String, String> uploadFile(HttpServletRequest request, @RequestParam("file") List<MultipartFile> files) throws Exception {
        Map<String, String> response = new HashMap<>();
        int i=1;
        if(files.size()>10) throw new Exception("Only 10 files can be uploaded at a time!!!");
        for(MultipartFile file: files) {
            User user = jwtUtil.getAuthenticatedUserFromContext();
            String path = user.getId() + "/" + request.getRequestURI().substring("/files/".length());
            if (path.startsWith("/")) throw new RuntimeException("Path is incorrect!!!");
            if (!path.endsWith("/")) path += "/";
            String fileName = file.getOriginalFilename();
            assert fileName != null;
            String encodedFilename = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            if(file.getSize()>=(10*1024*1024)) throw new Exception("File Size for the File :" + fileName+"exceeds the limit!");

            System.out.println("Encoded filename: " + encodedFilename);
            /*if(fileName != null && fileName.contains(" ")) throw new Exception("File Name is invalid!!!");*/
            if (folderRepository.findByPathAndActiveTrue(path + fileName + "/").isPresent()) {
                throw new Exception("File Already exists with the same name: " + fileName);
            }
            Path rootLocation = Paths.get("Uploads");
            Path uploadDir = Paths.get(rootLocation.toString(), path);
            Path filePath = uploadDir.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Folder fileEntity = Folder.builder()
                    .name(fileName)
                    .type(Folder.FolderType.FILE)
                    .createdAt(LocalDateTime.now())
                    .active(true)
                    .user(user)
                    .path(path + fileName + "/")
                    .parent(folderRepository.findByPathAndActiveTrue(path).orElseThrow(() -> new RuntimeException("Path not found!!!"))).build();
            folderRepository.save(fileEntity);


            response.put("message", "File uploaded successfully");
            response.put("fileName "+i++, fileName);
        }
        return response;
    }

    @GetMapping(value = {"/**","/"})
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) throws Exception {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        String path = user.getId() + "/" + request.getRequestURI().substring("/files/".length());
        path = java.net.URLDecoder.decode(path, StandardCharsets.UTF_8);
        if (path.startsWith("/")) throw new RuntimeException("Path is incorrect!!!");
        if (!path.endsWith("/")) path += "/";
        Folder fileEntity = folderRepository.findByPathAndActiveTrue(path)
                .orElseThrow(() -> new RuntimeException("File not found"));
        if(fileEntity.getType()== Folder.FolderType.FOLDER) throw new Exception("Folder cannot be Downloaded");
        Path rootLocation = Paths.get("Uploads");
        Path uploadDir = Paths.get(rootLocation.toString(),fileEntity.getPath()).normalize();
        String contentType = Files.probeContentType(uploadDir);
        if (contentType == null) {
            contentType = "application/octet-stream"; // Fallback to default
        }
        Resource resource;
        if (Files.exists(uploadDir) && Files.isReadable(uploadDir)) {
            resource = new UrlResource(uploadDir.toUri());
        } else {
            throw new IOException("File not found or unreadable: " + fileEntity.getName());
        }


        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getName()+ "\"")
                .body(resource);
    }
}
