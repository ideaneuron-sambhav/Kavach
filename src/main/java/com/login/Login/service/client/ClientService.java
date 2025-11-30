package com.login.Login.service.client;

import com.login.Login.dto.Response;
import com.login.Login.dto.clients.*;
import com.login.Login.dto.groups.GroupsResponse;
import com.login.Login.dto.user.UserResponse;
import com.login.Login.entity.*;
import com.login.Login.repository.*;
import com.login.Login.security.JwtUtil;
import com.login.Login.service.email.EmailService;
import com.login.Login.service.folder.FolderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.security.SecureRandom;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClientService {
    @Autowired
    FolderService folderService;
    @Autowired
    ClientRepository clientRepo;
    @Autowired
    UserRepository userRepo;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    ClientRepository clientRepository;
/*    @Autowired
    CredentialsRepository credentialsRepository;*/
    @Autowired
    GroupsRepository groupsRepository;
    @Autowired
    BCryptPasswordEncoder passwordEncoder;
    @Autowired
    EmailService emailService;
    @Autowired
    RoleRepository roleRepository;


    @Transactional
    public Response<ClientResponse> addClient(ClientRequest request) {
        jwtUtil.ensureAdminFromContext();
        Groups groups = null;
        if(request.getGroupId() != null){
            groups = groupsRepository.findById(request.getGroupId()).orElseThrow(()-> new RuntimeException("Group not exists"));
        }

        if(clientRepository.existsByAlias(request.getAlias())){
            throw new RuntimeException("Alias name already exists");
        }

        if (clientRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new RuntimeException("Client with this mobile number already exists");
        }
        User user = registerClient(request);
        Clients client = Clients.builder()
                .userId(user)
                .alias(request.getAlias())
                .mobileNumber(request.getMobileNumber())
                .address(request.getAddress())
                .groups(groups)
                .notes(request.getNotes())
                .type(request.getType())
                .active(request.getActive() != null ? request.getActive() : true)
                .details(request.getDetails())
                .build();

        Clients saved = clientRepository.save(client);

        return Response.<ClientResponse>builder()
                .data(toResponse(saved))
                .httpStatusCode(HttpStatus.CREATED.value())
                .message("Password sent to registered email !!!")
                .build();
    }
    // List all clients filter By group
    public Response<Page<ClientResponse>> listClientsByGroups(Long groupId, String keyword, int page, int size) {
        User currentUser = jwtUtil.getAuthenticatedUserFromContext();
        boolean isAdmin = jwtUtil.isAdminFromContext();
        Pageable pageable = PageRequest.of(page, size);
        Groups group = groupsRepository.findById(groupId).orElseThrow(()-> new RuntimeException("Group not exists with ID"));
        Page<Clients> pageResult;
        String searchTerm = (keyword == null || keyword.trim().isEmpty()) ? "" : keyword.trim();

        if (isAdmin) {
            // Admin can view all clients
            if (searchTerm.isEmpty()) {
                pageResult = clientRepository.findByGroups(group, pageable);
            } else {
                pageResult = clientRepository.searchByNameOrEmailAndGroup(group, searchTerm, pageable);
            }

        } else {
            // User can view only assigned clients
            pageResult = clientRepository.searchByAssignedClientsAndGroups(currentUser, group, searchTerm, pageable);
        }
        Page<ClientResponse> responsePage = pageResult.map(this::toResponse);

        return Response.<Page<ClientResponse>>builder()
                .data(responsePage)
                .httpStatusCode(HttpStatus.OK.value())
                .message("Client list fetched successfully for group: "+ group.getName())
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



    public Response<Map<String, Object>> viewClientNotes(Long id) {
        Clients client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));
        ClientResponse cr =  ClientResponse.builder().notes(client.getNotes()).build();
        String str ="";
        if(cr.getNotes()!=null) str = cr.getNotes();
        return Response.<Map<String, Object>>builder()
                .data(Map.of("notes",str))
                .httpStatusCode(HttpStatus.OK.value())
                .message("Client Notes fetched successfully")
                .build();
    }

    public Response<Map<String, Object>> updateClientNotes(Long id, String notes) {
        jwtUtil.ensureAdminFromContext();
        Clients client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));
        if (notes != null) client.setNotes(notes);
        Clients updated = clientRepository.save(client);

        return Response.<Map<String, Object>>builder()
                .data(Map.of("notes",updated.getNotes()))
                .httpStatusCode(HttpStatus.OK.value())
                .message("Client Notes updated successfully")
                .build();
    }

    // ✏Update client
    public Response<ClientResponse> updateClient(Long id, ClientRequest request) {
        jwtUtil.ensureAdminFromContext();
        Clients client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + id));
        User user = client.getUserId();
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepo.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getMobileNumber() != null && !request.getMobileNumber().equals(client.getMobileNumber())) {
            if (clientRepository.existsByMobileNumber(request.getMobileNumber())) {
                throw new RuntimeException("Mobile number already exists");
            }
            client.setMobileNumber(request.getMobileNumber());
        }

        if(request.getAlias() != null && !request.getAlias().equals(client.getAlias())) {
            if(clientRepository.existsByAlias(request.getAlias())){
                throw new RuntimeException("Alias Name already exists");
            }
            client.setAlias(request.getAlias());
        }

        if (request.getAddress() != null) client.setAddress(request.getAddress());
        if(request.getDetails() != null) client.setDetails(request.getDetails());


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

        String status = updated.getActive() ? "activated" : "deactivated";

        return Response.<ClientResponse>builder()
                .data(toResponse(updated))
                .httpStatusCode(HttpStatus.OK.value())
                .message("Client " + status + " successfully")
                .build();
    }
    @Transactional
    public Response<String> assignClientToUser(Long clientId, Long userId) throws Exception {
        // Only admin can assign
        jwtUtil.ensureAdminFromContext();

        Clients clients = clientRepo.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (userId == -1) {
            clients.setAssignedUser(null);
            clientRepo.save(clients);
            return Response.<String>builder()
                    .data(null)
                    .httpStatusCode(200)
                    .message("User unassigned from client " + clients.getUserId().getFirstName() + " " + clients.getUserId().getLastName())
                    .build();
        }

        // Assign user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(user.getRole().getName().equalsIgnoreCase("admin")){
            throw new Exception("Client cannot be assigned to ADMIN!");
        }
        clients.setAssignedUser(user);
        clientRepo.save(clients);

        return Response.<String>builder()
                .data(null)
                .httpStatusCode(200)
                .message("Client " + clients.getUserId().getFirstName() + " " + clients.getUserId().getLastName() + " assigned to user " + user.getFirstName())
                .build();
    }


    // Helper: Convert Entity → Response DTO
    private ClientResponse toResponse(Clients client) {
        UserResponse userResponse = null;
        GroupsResponse groupsResponse = null;
        if(client.getGroups() != null){
            groupsResponse = GroupsResponse.builder()
                    .id(client.getGroups().getId())
                    .name(client.getGroups().getName())
                    .alias(client.getGroups().getAlias())
                    .representativeName(client.getGroups().getRepresentativeName())
                    .email(client.getGroups().getEmail())
                    .mobileNumber(client.getGroups().getMobileNumber())
                    .active(client.getGroups().isActive())
                    .build();
        }

        if (client.getAssignedUser() != null) {
            userResponse= UserResponse.builder()
                    .id(client.getAssignedUser().getId())
                    .firstName(client.getAssignedUser().getFirstName())
                    .lastName(client.getAssignedUser().getLastName())
                    .active(client.getAssignedUser().getActive())
                    .build();
        }

        return ClientResponse.builder()
                .id(client.getId())
                .firstName(client.getUserId().getFirstName())
                .lastName(client.getUserId().getLastName())
                .alias(client.getAlias())
                .email(client.getUserId().getEmail())
                .mobileNumber(client.getMobileNumber())
                .address(client.getAddress())
                .type(client.getType())
                .groups(groupsResponse)
                .active(client.getActive())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .details(client.getDetails())
                .assignedUser(userResponse)
                .build();
    }
    @Transactional
    private User registerClient(ClientRequest request){
        try {
            jwtUtil.ensureAdminFromContext();

            // Validate required fields
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new RuntimeException("Email cannot be empty");
            }

            if (request.getFirstName() == null || request.getFirstName().isBlank()) {
                throw new RuntimeException("First name cannot be empty");
            }

            // Check if email already exists
            if (userRepo.findByEmail(request.getEmail().toLowerCase()).isPresent()) {
                throw new RuntimeException("Email already registered in User Panel");
            }

            String password = generatePassword(request.getFirstName());
            String encodedPassword = passwordEncoder.encode(password);
            System.out.println(password);
            //emailService.sendPasswordEmail(request.getEmail(), password);
/*          Assigning role to admin if its null or blank
            String roleName = request.getRole()!= null ? request.getRole() : "user";
            Role role = roleRepository.findByNameIgnoreCase(roleName)
            .orElseThrow(()-> new RuntimeException("Role not found: "+ roleName));
*/          Role role = roleRepository.findByNameIgnoreCase("clients")
                    .orElseThrow(() -> new RuntimeException("Role not found: " + "Clients"));

            // Create user
            User user = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail().toLowerCase())
                    .role(role)
                    .password(encodedPassword)
                    .active(true)
                    .build();

            userRepo.save(user);
            Folder folder = folderService.createUserRootFolder(user.getId());
            user.setRootFolder(folder);
            userRepo.save(user);

            return user;


        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Database constraint violation: " + e.getMostSpecificCause().getMessage());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error registering user: " + e.getMessage());
        }
    }
    private String generatePassword(String name) {

        String clean = name.trim().toUpperCase();

        // First four letters (pad with x if less than 4)
        String firstFour = clean.length() >= 4 ? clean.substring(0, 4) : String.format("%-4s", clean).replace(' ', 'X');

        // Generate 4 random digits
        SecureRandom random = new SecureRandom();
        int digits = 1000 + random.nextInt(9000);

        return firstFour + digits;
    }

}

