package com.login.Login.service.folder;

import com.login.Login.dto.Response;
import com.login.Login.dto.folder.FolderResponse;
import com.login.Login.entity.*;
import com.login.Login.repository.*;
import com.login.Login.security.JwtUtil;
import com.login.Login.service.filesystemservice.FileSystemService;
import jakarta.transaction.Transactional;
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
public class FolderService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    FolderRepository folderRepository;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    FileSystemService fileSystemService;


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
    public Response<Object> createFolder(String path, User user) throws Exception {
        jwtUtil.getAuthenticatedUserFromContext();
        if (path.startsWith("/")) throw new RuntimeException("Path is incorrect!!!");
        if (!path.endsWith("/")) path += "/";
        path = java.net.URLDecoder.decode(path,StandardCharsets.UTF_8);
        String[] folderNames = path.split("/");
        String name = folderNames[folderNames.length - 1];
        String parentFolderPath = path.substring(0,(path.length()-name.length()-1));

        Folder parentFolder = folderRepository.findByPathAndActiveTrue(parentFolderPath).orElseThrow(() -> new RuntimeException("Folder Cannot be created in sequence!!!"));
        if(parentFolder.getType()== Folder.FolderType.FILE) throw new Exception("Folder cannot be created in File!!!");
        String newPath = parentFolder.getPath()+name+"/";
        if(folderRepository.findByPathAndActiveTrue(parentFolder.getPath()+name.toLowerCase()+"/").isPresent()) throw new Exception("Folder Already Exists with the Same Name in Lower Case");
        if(folderRepository.findByPathAndActiveTrue(parentFolder.getPath()+name.toUpperCase()+"/").isPresent()) throw new Exception("Folder Already Exists with the Same Name in Upper Case");

        newPath = java.net.URLDecoder.decode(newPath, StandardCharsets.UTF_8);
        name = java.net.URLDecoder.decode(name, StandardCharsets.UTF_8);
        if(folderRepository.findByPathAndActiveTrue(newPath).isPresent()){
            throw new RuntimeException("Folder already exist with the same name : " + name);
        }

        Folder folder = Folder.builder()
                .name(name.toLowerCase())
                .type(Folder.FolderType.FOLDER)
                .path(newPath.toLowerCase())
                .parent(parentFolder)
                .user(user)
                .active(true)
                .build();
        folderRepository.save(folder);
        fileSystemService.createFolder(parentFolder.getPath(), name.toLowerCase());
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
    public Response<Object> updateFolder(String path,  String name, User user) throws Exception {

        jwtUtil.getAuthenticatedUserFromContext();
        if (path.startsWith("/")) throw new RuntimeException("Path is incorrect!!!");
        if (!path.endsWith("/")) path += "/";
        path = java.net.URLDecoder.decode(path,StandardCharsets.UTF_8);
        String[] folderNames = path.split("/");
        String newFolder = folderNames[folderNames.length - 1];
        String parentFolder = path.substring(0,(path.length()-newFolder.length()-1));
        Folder folder = folderRepository.findByPathAndActiveTrue(path).orElseThrow(() -> new RuntimeException("Folder not found for the specific name!!!"));
        if(folder.getType()== Folder.FolderType.FILE) throw new Exception("File name cannot be changed in this API!!!");
        path = parentFolder+name.toLowerCase()+"/";
        if(folderRepository.findByPathAndActiveTrue(path.toLowerCase()+"/").isPresent()) throw new Exception("Folder Already Exists with the Same Name in Lower Case");
        if(folderRepository.findByPathAndActiveTrue(path.toUpperCase()+"/").isPresent()) throw new Exception("Folder Already Exists with the Same Name in Upper Case");
        name = name.toLowerCase();
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
        jwtUtil.getAuthenticatedUserFromContext();
        if (path.startsWith("/")) throw new RuntimeException("Path is incorrect!!!");
        if (!path.endsWith("/")) path += "/";
        path = java.net.URLDecoder.decode(path,StandardCharsets.UTF_8);

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
        jwtUtil.getAuthenticatedUserFromContext();
        if (path.length() == 2) throw new RuntimeException("Main Folder cannot be deleted");
        if (path.startsWith("/")) throw new RuntimeException("Path is incorrect!!!");
        if (!path.endsWith("/")) path += "/";
        path = java.net.URLDecoder.decode(path, StandardCharsets.UTF_8);

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
