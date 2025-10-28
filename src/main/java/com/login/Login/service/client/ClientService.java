package com.login.Login.service.client;

import com.login.Login.dto.Response;
import com.login.Login.dto.clients.*;
import com.login.Login.dto.nominee.*;
import com.login.Login.dto.user.UserResponse;
import com.login.Login.entity.Clients;
import com.login.Login.entity.Nominee;
import com.login.Login.entity.User;
import com.login.Login.repository.ClientRepository;
import com.login.Login.repository.CredentialsRepository;
import com.login.Login.repository.UserRepository;
import com.login.Login.security.JwtUtil;
import com.login.Login.service.credentials.CredentialsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepo;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final ClientRepository clientRepository;
    private final CredentialsRepository credentialsRepository;

    // Add new client
    public Response<ClientResponse> addClient(ClientRequest request) {
        jwtUtil.ensureAdminFromContext();
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Client with this email already exists");
        }

        if (clientRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new RuntimeException("Client with this mobile number already exists");
        }

        Nominee nominee = null;
        if (request.getNominee() != null) {
            NomineeRequest n = request.getNominee();
            nominee = Nominee.builder()
                    .name(n.getName())
                    .relation(n.getRelation())
                    .mobileNumber(n.getMobileNumber())
                    .age(n.getAge())
                    .build();
        }

        Clients client = Clients.builder()
                .name(request.getName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .address(request.getAddress())
                .active(request.getActive() != null ? request.getActive() : true)
                .nominee(nominee)
                .build();

        Clients saved = clientRepository.save(client);

        return Response.<ClientResponse>builder()
                .data(toResponse(saved))
                .httpStatusCode(HttpStatus.CREATED.value())
                .message("Client created successfully")
                .build();
    }

    // List all clients
    public Response<Page<ClientResponse>> listClients(String keyword, int page, int size) {
        User currentUser = jwtUtil.getAuthenticatedUserFromContext();
        boolean isAdmin = jwtUtil.isAdminFromContext();
        Pageable pageable = PageRequest.of(page, size);
        Page<Clients> pageResult;
        String searchTerm = (keyword == null || keyword.trim().isEmpty()) ? "" : keyword.trim();

        if (isAdmin) {
            // Admin can view all clients
            if (searchTerm.isEmpty()) {
                pageResult = clientRepository.findAll(pageable);
            } else {
                pageResult = clientRepository.searchByNameOrEmail(searchTerm, pageable);
            }
        } else {
            // User can view only assigned clients
            pageResult = clientRepository.searchAssignedClients(currentUser, searchTerm, pageable);
        }
        Page<ClientResponse> responsePage = pageResult.map(this::toResponse);

        return Response.<Page<ClientResponse>>builder()
                .data(responsePage)
                .httpStatusCode(HttpStatus.OK.value())
                .message("Client list fetched successfully")
                .build();
    }

    // ✏Update client
    public Response<ClientResponse> updateClient(Long id, ClientRequest request) {
        jwtUtil.ensureAdminFromContext();
        Clients client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(client.getEmail())) {
            if (clientRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            client.setEmail(request.getEmail());
        }

        if (request.getMobileNumber() != null && !request.getMobileNumber().equals(client.getMobileNumber())) {
            if (clientRepository.existsByMobileNumber(request.getMobileNumber())) {
                throw new RuntimeException("Mobile number already exists");
            }
            client.setMobileNumber(request.getMobileNumber());
        }

        if (request.getName() != null) client.setName(request.getName());
        if (request.getAddress() != null) client.setAddress(request.getAddress());

        if (request.getNominee() != null) {
            if (client.getNominee() == null) {
                client.setNominee(new Nominee());
            }
            Nominee nominee = client.getNominee();
            NomineeRequest n = request.getNominee();

            if (n.getName() != null) nominee.setName(n.getName());
            if (n.getRelation() != null) nominee.setRelation(n.getRelation());
            if (n.getMobileNumber() != null) nominee.setMobileNumber(n.getMobileNumber());
            if(n.getAge() != null) nominee.setAge(n.getAge());

        }

        Clients updated = clientRepository.save(client);

        return Response.<ClientResponse>builder()
                .data(toResponse(updated))
                .httpStatusCode(HttpStatus.OK.value())
                .message("Client updated successfully")
                .build();
    }

    // Toggle active/inactive
    public Response<ClientResponse> toggleActiveStatus(Long id) {
        jwtUtil.ensureAdminFromContext();
        Clients client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));

        client.setActive(!client.getActive());
        Clients updated = clientRepository.save(client);
        if (client.getActive()) {
            credentialsRepository.activateAllByClientId(updated.getId());
        } else {
            credentialsRepository.deactivateAllByClientId(updated.getId());
        }



        String status = updated.getActive() ? "activated" : "deactivated";

        return Response.<ClientResponse>builder()
                .data(toResponse(updated))
                .httpStatusCode(HttpStatus.OK.value())
                .message("Client " + status + " successfully")
                .build();
    }
    @Transactional
    public Response<String> assignClientToUser(AssignClientRequest request) {
        // Only admin can assign
        jwtUtil.ensureAdminFromContext();

        Clients clients = clientRepo.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (request.getUserId() == null) {
            // Unassign user
            clients.setAssignedUser(null);
            clientRepo.save(clients);
            return Response.<String>builder()
                    .data(null)
                    .httpStatusCode(200)
                    .message("User unassigned from client " + clients.getName())
                    .build();
        }

        // Assign user
        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        clients.setAssignedUser(user);
        clientRepo.save(clients);

        return Response.<String>builder()
                .data(null)
                .httpStatusCode(200)
                .message("Client " + clients.getName() + " assigned to user " + user.getFirstName())
                .build();
    }


    // Helper: Convert Entity → Response DTO
    private ClientResponse toResponse(Clients client) {
        NomineeResponse nomineeResponse = null;
        UserResponse userResponse = null;
        if (client.getNominee() != null) {
            nomineeResponse = NomineeResponse.builder()
                    .name(client.getNominee().getName())
                    .relation(client.getNominee().getRelation())
                    .mobileNumber(client.getNominee().getMobileNumber())
                    .age(client.getNominee().getAge())
                    .build();
        }
        if (client.getAssignedUser() != null) {
            userResponse= UserResponse.builder()
                    .id(client.getAssignedUser().getId())
                    .email(client.getAssignedUser().getEmail())
                    .active(client.getAssignedUser().getActive())
                    .build();
        }

        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .mobileNumber(client.getMobileNumber())
                .address(client.getAddress())
                .active(client.getActive())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .nominee(nomineeResponse)
                .user(userResponse)
                .build();
    }
}

