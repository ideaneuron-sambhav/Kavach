package com.login.Login.controller.folder;

import com.login.Login.dto.folder.FolderResponse;
import com.login.Login.entity.*;
import com.login.Login.repository.*;
import com.login.Login.security.JwtUtil;
import com.login.Login.service.folder.FolderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/folders")
public class FolderController {
    @Autowired
    FolderService folderService;
    @Autowired
    FolderRepository folderRepository;
    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping({"/**", "/"})
    public FolderResponse createFolders(HttpServletRequest request) throws Exception {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        String path = user.getId() + "/" + request.getRequestURI().substring("/folders/".length());
        if (path.startsWith("/")) throw new RuntimeException("Path is incorrect!!!");
        if (!path.endsWith("/")) path += "/";
        String[] folderNames = path.split("/");
        String newFolder = folderNames[folderNames.length - 1];
        String parentFolder = path.substring(0,(path.length()-newFolder.length()-1));

        Folder folder = folderRepository.findByPathAndActiveTrue(parentFolder).orElseThrow(() -> new RuntimeException("Folder Cannot be created in sequence!!!"));
        if(folder.getType()== Folder.FolderType.FILE) throw new Exception("Folder cannot be created in File!!!");
        String newPath = folder.getPath()+newFolder+"/";
        if(folderRepository.findByPathAndActiveTrue(folder.getPath()+newFolder.toLowerCase()+"/").isPresent()) throw new Exception("Folder Already Exists with the Same Name in Lower Case");
        if(folderRepository.findByPathAndActiveTrue(folder.getPath()+newFolder.toUpperCase()+"/").isPresent()) throw new Exception("Folder Already Exists with the Same Name in Upper Case");
        return folderService.createFolder(newFolder, folder, newPath);

    }

    @PatchMapping(value = {"/**", "/"})
    public FolderResponse renameFolders(HttpServletRequest request, @RequestParam(name = "name", required = true) String name) throws Exception {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        String path = user.getId() + "/" + request.getRequestURI().substring("/folders/".length());
        if (path.startsWith("/")) throw new RuntimeException("Path is incorrect!!!");
        if (!path.endsWith("/")) path += "/";
        String[] folderNames = path.split("/");
        String newFolder = folderNames[folderNames.length - 1];
        String parentFolder = path.substring(0,(path.length()-newFolder.length()-1));
        Folder folder = folderRepository.findByPathAndActiveTrue(path).orElseThrow(() -> new RuntimeException("Folder not found for the specific name!!!"));
        if(folder.getType()== Folder.FolderType.FILE) throw new Exception("File name cannot be changed in this API!!!");
        String newPath = parentFolder+name+"/";
        if(folderRepository.findByPathAndActiveTrue(newPath.toLowerCase()+"/").isPresent()) throw new Exception("Folder Already Exists with the Same Name in Lower Case");
        if(folderRepository.findByPathAndActiveTrue(newPath.toUpperCase()+"/").isPresent()) throw new Exception("Folder Already Exists with the Same Name in Upper Case");
        return folderService.updateFolder(name, folder, newPath);

    }

    @GetMapping("/listAll")
    public List<?> listAll(){
        return folderService.listAll();
    }

    @GetMapping(value = {"/**", "/"})
    public List<?> list(HttpServletRequest request) throws Exception {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        String path = user.getId() + "/" + request.getRequestURI().substring("/folders/".length());
        if (path.startsWith("/")) throw new RuntimeException("Path is incorrect!!!");
        if (!path.endsWith("/")) path += "/";

        return folderService.list(path);
    }


    @PutMapping(value = {"/**", "/"})
    public String deleteFolderAndFiles(HttpServletRequest request) {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        String path = user.getId() + "/" + request.getRequestURI().substring("/folders/".length());
        if (path.length() == 2) throw new RuntimeException("Main Folder cannot be deleted");
        if (path.startsWith("/")) throw new RuntimeException("Path is incorrect!!!");
        if (!path.endsWith("/")) path += "/";
        path = java.net.URLDecoder.decode(path, StandardCharsets.UTF_8);
        if(folderService.deleteFolder(path)) {
            return "Deleted successfully!!!";
        }else {
            return "Error while deleting!!";
        }
    }


}
