package com.login.Login.controller.folder;

import com.login.Login.dto.Response;
import com.login.Login.entity.*;
import com.login.Login.repository.*;
import com.login.Login.security.JwtUtil;
import com.login.Login.service.folder.FolderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/folders/")
public class FolderController {
    @Autowired
    FolderService folderService;
    @Autowired
    JwtUtil jwtUtil;


    @PostMapping({"/**", "/"})
    public Response<?> createFolders(HttpServletRequest request) throws Exception {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        String path = user.getId() + "/" + request.getRequestURI().substring("/folders/".length());
        return folderService.createFolder(path, user);

    }

    @PatchMapping(value = {"/**", "/"})
    public Response<?> renameFolders(HttpServletRequest request, @RequestParam(name = "name") String name) throws Exception {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        String path = user.getId() + "/" + request.getRequestURI().substring("/folders/".length());
        return folderService.updateFolder(path, name, user);

    }

    @GetMapping("/listAll")
    public List<?> listAll(){
        return folderService.listAll();
    }

    @GetMapping(value = {"/**", "/"})
    public List<?> list(HttpServletRequest request) throws Exception {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        String path = user.getId() + "/" + request.getRequestURI().substring("/folders/".length());
        return folderService.list(path);
    }


    @PutMapping(value = {"/**", "/"})
    public String deleteFolderAndFiles(HttpServletRequest request){
        User user = jwtUtil.getAuthenticatedUserFromContext();
        String path = user.getId() + "/" + request.getRequestURI().substring("/folders/".length());
        if(folderService.deleteFolder(path)) {
            return "Deleted successfully!!!";
        }else {
            return "Error while deleting!!";
        }
    }


}
