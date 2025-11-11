package com.login.Login.service.folder;

import com.login.Login.dto.Response;
import com.login.Login.dto.folder.FolderResponse;
import com.login.Login.entity.*;
import com.login.Login.repository.*;
import com.login.Login.security.JwtUtil;
import com.login.Login.service.filesystemservice.FileSystemService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@AllArgsConstructor
public class FolderService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private FileSystemService fileSystemService;


    public Folder createUserRootFolder(Long userId) throws IOException {
        jwtUtil.getAuthenticatedUserFromContext();
        String folderName = String.valueOf(userId);
        String path = folderName+"/";
        if(folderRepository.findByNameAndActiveTrue(folderName).isPresent()){
            throw new RuntimeException("Folder already exists");
        }

        Folder folder = Folder.builder()
                .name(folderName)
                .type(Folder.FolderType.FOLDER)
                .path(path)
                .active(true)
                .user(userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not exist!!!")))
                .active(true)
                .build();
        Path binLocation = Paths.get("Recycle Bin");
        Path rootLocation = Paths.get("Uploads");
        Path parentDir = Paths.get(rootLocation.toString(), folderName);
        Files.createDirectories(parentDir);
        Path binDir = Paths.get(binLocation.toString(), folderName);
        Files.createDirectories(binDir);

        return folderRepository.save(folder);
    }
    @Transactional
    public Response<Object> createFolder(String name, Folder parentFolder, String path) {

        User user = jwtUtil.getAuthenticatedUserFromContext();
        path = java.net.URLDecoder.decode(path, StandardCharsets.UTF_8);
        name = java.net.URLDecoder.decode(name, StandardCharsets.UTF_8);
        if(folderRepository.findByPathAndActiveTrue(path).isPresent()){
            throw new RuntimeException("Folder already exist with the same name : " + name);
        }

        Folder folder = Folder.builder()
                .name(name)
                .type(Folder.FolderType.FOLDER)
                .path(path)
                .parent(parentFolder)
                .user(user)
                .active(true)
                .build();
        folderRepository.save(folder);
        fileSystemService.createFolder(parentFolder.getPath(), name);
        return Response.builder()
                .data(FolderResponse.builder()
                .folderId(folder.getId())
                .type(folder.getType().name())
                .parentId(folder.getParent().getId())
                .name(folder.getName())
                .userId(user.getId())
                .path(folder.getPath().substring(String.valueOf(user.getId()).length()+1))
                .createdAt(folder.getCreatedAt())
                .build())
                .httpStatusCode(HttpStatus.OK.value())
                .message("The folder created successfully: " + folder.getName())
                .build();
    }

    @Transactional
    public Response<Object> updateFolder(String name, Folder folder, String path) throws IOException {

        User user = jwtUtil.getAuthenticatedUserFromContext();
        path = URLDecoder.decode(path, StandardCharsets.UTF_8);
        name = URLDecoder.decode(name, StandardCharsets.UTF_8);
        if(folderRepository.findByPathAndActiveTrue(path).isPresent()){
            throw new RuntimeException("Folder already exist with the same name : " + name);
        }
        String oldPath = "Uploads/" + folder.getPath();
        Path oldDir = Paths.get(oldPath);
        String newPath = "Uploads/"+path;
        Path newDir = Paths.get(newPath);
        Files.move(oldDir, newDir, StandardCopyOption.ATOMIC_MOVE);
        updateFolderAndChildren(folder, folder.getPath(), path);
        Folder updateFolder = Folder.builder()
                .id(folder.getId())
                .name(name)
                .type(Folder.FolderType.FOLDER)
                .path(path)
                .parent(folder.getParent())
                .user(user)
                .createdAt(folder.getCreatedAt())
                .active(true)
                .build();
        folderRepository.save(updateFolder);
        return Response.builder()
                .data(FolderResponse.builder()
                .folderId(updateFolder.getId())
                .type(updateFolder.getType().name())
                .parentId(updateFolder.getParent().getId())
                .name(updateFolder.getName())
                .userId(user.getId())
                .path(updateFolder.getPath().substring(String.valueOf(user.getId()).length()+1))
                .createdAt(updateFolder.getCreatedAt())
                .build())
                .httpStatusCode(HttpStatus.OK.value())
                .message("The Folder Update successfully!!!")
                .build();
    }
    @Transactional
    private void updateFolderAndChildren(Folder folder, String oldBasePath, String newBasePath) {
        String updatedPath = folder.getPath().replaceFirst(oldBasePath, newBasePath);
        folder.setPath(updatedPath);

        if (folder.getPath().equals(newBasePath)) {
            folder.setName(Paths.get(newBasePath).getFileName().toString());
        }

        List<Folder> children = folderRepository.findByParentIdAndActiveTrue(folder.getId());
        for (Folder child : children) {
            updateFolderAndChildren(child, oldBasePath, newBasePath);
        }

        folderRepository.save(folder);
    }

    public List<FolderResponse> list(String path) throws Exception {
        Folder folder = folderRepository.findByPathAndActiveTrue(path).orElseThrow(() -> new RuntimeException("Error"));
        if(folder.getType()==Folder.FolderType.FILE) throw new Exception("Path must be a Folder Not a FILE!!!");
        List<Folder> listResult = folderRepository.findByParentIdAndActiveTrue(folder.getId());
        return listResult.stream().map(FolderResponse::from).toList();
    }

    public List<FolderResponse> listAll(){
        Long userId = jwtUtil.getUserIdFromContext();
        String path = userId + "/";
        List<Folder> listResult = folderRepository.findByEntityBIdCustomQuery(userId, path);
        return listResult.stream().map(FolderResponse::from).toList();
    }


    @Transactional
    public boolean deleteFolder(String path) {

        Folder folder = folderRepository.findByPathAndActiveTrue(path)
                .orElseThrow(() -> new RuntimeException("Folder Or File not found"));
        if(folder.getType()== Folder.FolderType.FOLDER){
            deleteSubFolders(folder);
        }
        folder.setActive(false);
        return fileSystemService.moveToBin(path);
    }

    @Transactional
    private void deleteSubFolders(Folder folder){
        List<Folder> subFolder = folderRepository.findByParentIdAndActiveTrue(folder.getId());
        for(Folder sub : subFolder){
            deleteSubFolders(sub);
        }
        folder.setActive(false);
        folderRepository.save(folder);
    }

}
