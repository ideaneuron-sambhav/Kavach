package com.login.Login.controller.client;

import com.login.Login.dto.Response;
import com.login.Login.dto.clients.ClientRequest;
import com.login.Login.dto.clients.ClientResponse;
import com.login.Login.service.client.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
    @Autowired
    ClientService clientService;

    // Add a new client
    @PostMapping("/add")
    public ResponseEntity<Response<ClientResponse>> addClient(@RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.addClient(request));
    }

    // List all clients
    @GetMapping("/list")
    public ResponseEntity<Response<Page<ClientResponse>>> listClients(@RequestParam(defaultValue = "") String search,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(clientService.listClients(search, page, size));
    }

    // Update client data
    @PutMapping("/update/{id}")
    public ResponseEntity<Response<ClientResponse>> updateClient(@PathVariable Long id,
            @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.updateClient(id, request));
    }

    // Assign a Client to Specific User using User_ID and Client_ID
    @PostMapping("/assign")
    public Response<?> assignClient(@RequestParam Long clientId,
                                    @RequestParam Long userId) throws Exception {
        if(clientId == null) throw new Exception("Client Id cannot be null");
        if(userId == null) throw new Exception("User Id cannot be null");
        return clientService.assignClientToUser(clientId, userId);
    }


    // Toggle client active/inactive
    @PutMapping("/toggle/{id}")
    public ResponseEntity<Response<ClientResponse>> toggleActiveStatus(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.toggleActiveStatus(id));
    }
}

