package com.login.Login.controller.client;

import com.login.Login.dto.Response;
import com.login.Login.dto.clients.ClientIdRequest;
import com.login.Login.entity.Clients;
import com.login.Login.entity.User;
import com.login.Login.repository.ClientRepository;
import com.login.Login.security.JwtUtil;
import com.login.Login.service.folder.FolderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/clients/folders/")
public class ClientFoldersAccess {
    @Autowired
    FolderService folderService;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    ClientRepository clientRepository;


    @PostMapping({"/**", "/"})
    public Response<?> createFolders(HttpServletRequest request, @RequestBody ClientIdRequest clientRequest) throws Exception {
        jwtUtil.ensureAdminFromContext();
        Clients client = clientRepository.findById(clientRequest.getClientId()).orElseThrow(()-> new RuntimeException("Client Not Found!!!"));
        String path = client.getUserId().getId() + "/" + request.getRequestURI().substring("/clients/folders/".length());
        return folderService.createFolder(path, client.getUserId());

    }

    @PatchMapping(value = {"/**", "/"})
    public Response<?> renameFolders(HttpServletRequest request, @RequestParam(name = "name") String name, @RequestBody ClientIdRequest clientRequest) throws Exception {
        jwtUtil.ensureAdminFromContext();
        Clients client = clientRepository.findById(clientRequest.getClientId()).orElseThrow(()-> new RuntimeException("Client Not Found!!!"));
        String path = client.getUserId().getId() + "/" + request.getRequestURI().substring("/clients/folders/".length());
        return folderService.updateFolder(path, name, client.getUserId());

    }

    @GetMapping(value = {"/**", "/"})
    public List<?> list(HttpServletRequest request, @RequestBody ClientIdRequest clientRequest) throws Exception {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        boolean isAdmin = jwtUtil.isAdminFromContext();
        Clients client = clientRepository.findById(clientRequest.getClientId()).orElseThrow(()-> new RuntimeException("Client Not Found!!!"));
        String path;
        if(isAdmin){
            path = client.getUserId().getId() + "/" + request.getRequestURI().substring("/clients/folders/".length());
        }else if(client.getAssignedUser() == user){
            path = client.getUserId().getId() + "/" + request.getRequestURI().substring("/clients/folders/".length());
        }else{
            throw new RuntimeException("Access Denied: Client not assigned to user");
        }
        return folderService.list(path);
    }


    @PutMapping(value = {"/**", "/"})
    public String deleteFolderAndFiles(HttpServletRequest request, @RequestBody ClientIdRequest clientRequest){
        jwtUtil.ensureAdminFromContext();
        Clients client = clientRepository.findById(clientRequest.getClientId()).orElseThrow(()-> new RuntimeException("Client Not Found!!!"));
        String path = client.getUserId().getId() + "/" + request.getRequestURI().substring("/clients/folders/".length());
        if(folderService.deleteFolder(path)) {
            return "Deleted successfully!!!";
        }else {
            return "Error while deleting!!";
        }
    }


}

