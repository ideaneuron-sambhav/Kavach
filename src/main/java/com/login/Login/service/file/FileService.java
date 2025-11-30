package com.login.Login.service.file;

import com.login.Login.dto.Response;
import com.login.Login.entity.Folder;
import com.login.Login.entity.User;
import com.login.Login.repository.FolderRepository;
import com.login.Login.repository.UserRepository;
import com.login.Login.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
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
import java.util.Objects;

@Service
public class FileService {
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    FolderRepository folderRepository;
    @Autowired
    UserRepository userRepository;

    @Transactional
    public Response<Object> uploadFile(String path, List<MultipartFile> files) throws IOException {
        Map<String, String> response = new HashMap<>();
        int i = 1;
        if (files.size() > 10) throw new RuntimeException("Only 10 files can be uploaded at a time!!!");
        for (MultipartFile file : files) {
            User user = jwtUtil.getAuthenticatedUserFromContext();


            if (path.startsWith("/")) throw new RuntimeException("Path is incorrect!!!");
            if (!path.endsWith("/")) path += "/";
            path = java.net.URLDecoder.decode(path, StandardCharsets.UTF_8);
            String fileName = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
            String encodedFilename = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            if (file.getSize() >= (10 * 1024 * 1024))
                throw new RuntimeException("File Size for the File :" + fileName + " exceeds the limit!");

            System.out.println("Encoded filename: " + encodedFilename);
            /*if(fileName != null && fileName.contains(" ")) throw new Exception("File Name is invalid!!!");*/
            if (folderRepository.findByPathAndActiveTrue(path + fileName + "/").isPresent()) {
                throw new RuntimeException("File Already exists with the same name: " + fileName);
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
            response.put("fileName " + i++, fileName);
        }
        return Response.builder()
                .data(response)
                .httpStatusCode(HttpStatus.OK.value())
                .message("File Uploaded Successfully")
                .build();
    }

    @Transactional
    public ResponseEntity<Resource> downloadFile(String path) throws IOException {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        path = java.net.URLDecoder.decode(path, StandardCharsets.UTF_8);
        if (path.startsWith("/")) throw new RuntimeException("Path is incorrect!!!");
        if (!path.endsWith("/")) path += "/";
        Folder fileEntity = folderRepository.findByPathAndActiveTrue(path)
                .orElseThrow(() -> new RuntimeException("File not found"));
        if(fileEntity.getType()== Folder.FolderType.FOLDER) throw new RuntimeException("Folder cannot be Downloaded");
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
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileEntity.getName()+ "\"")
                .body(resource);
    }

}
