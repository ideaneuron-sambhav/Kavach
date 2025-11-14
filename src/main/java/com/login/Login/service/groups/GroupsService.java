package com.login.Login.service.groups;


import com.login.Login.dto.Response;
import com.login.Login.dto.groups.GroupsRequest;
import com.login.Login.dto.groups.GroupsResponse;
import com.login.Login.entity.Groups;
import com.login.Login.entity.User;
import com.login.Login.repository.GroupsRepository;
import com.login.Login.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class GroupsService {

    @Autowired
    GroupsRepository groupsRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Transactional
    public Response<GroupsResponse> registerGroups(GroupsRequest request) {
        User user = jwtUtil.getAuthenticatedUserFromContext();
        jwtUtil.ensureAdminFromContext();

        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Groups cannot be empty");
        }

        if (groupsRepository.findAllByName(request.getName()).isPresent()) {
            throw new RuntimeException("Group already exists: " + request.getName());
        }
        if(groupsRepository.existsByAlias(request.getAlias())){
            throw new RuntimeException("Group Alias Name already exists" + request.getAlias());
        }

        Groups groups = Groups.builder()
                .name(request.getName())
                .alias(request.getAlias())
                .representativeName(request.getRepresentativeName())
                .mobileNumber(request.getMobileNumber())
                .email(request.getEmail())
                .active(true)
                .build();

        Groups savedGroup = groupsRepository.save(groups);



        return Response.<GroupsResponse>builder()
                .data(toResponse(savedGroup))
                .httpStatusCode(201)
                .message("Groups added successfully by admin: " + user.getEmail())
                .build();
    }


    public Response<Page<GroupsResponse>> listGroups(String keyword, int page, int size) {
        jwtUtil.ensureAdminFromContext();
        Pageable pageable = PageRequest.of(page, size);
        Page<Groups> pageResult;
        String searchTerm = (keyword == null || keyword.trim().isEmpty()) ? "" : keyword.trim();

            if (searchTerm.isEmpty()) {
                pageResult = groupsRepository.findAll(pageable);
            } else {
                pageResult = groupsRepository.searchByNameOrRepresentativeName(searchTerm, pageable);
            }

        Page<GroupsResponse> responsePage = pageResult.map(this::toResponse);

        return Response.<Page<GroupsResponse>>builder()
                .data(responsePage)
                .httpStatusCode(HttpStatus.OK.value())
                .message("Groups list fetched successfully")
                .build();
    }

    @Transactional
    public Response<GroupsResponse> updateGroups(Long id, GroupsRequest request) {
        jwtUtil.ensureAdminFromContext();
        Groups groups = groupsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with ID: " + id));

        if (request.getName() != null && !request.getName().equals(groups.getName())) {
            if (groupsRepository.existsByName(request.getName())) {
                throw new RuntimeException("Name already exists");
            }
            groups.setName(request.getName());
        }
        if(request.getAlias() != null){
            if(groupsRepository.existsByAlias(request.getAlias())){
                throw new RuntimeException("Alias Name already exists");
            }
            groups.setAlias(request.getAlias());
        }


        if (request.getRepresentativeName() != null) groups.setName(request.getName());
        if (request.getEmail() != null) groups.setEmail(request.getEmail());
        if(request.getMobileNumber() != null) groups.setMobileNumber(request.getMobileNumber());

        Groups updated = groupsRepository.save(groups);

        return Response.<GroupsResponse>builder()
                .data(toResponse(updated))
                .httpStatusCode(HttpStatus.OK.value())
                .message("Group updated successfully")
                .build();
    }

    // Toggle active/inactive
    public Response<GroupsResponse> toggleActiveStatus(Long id) {
        jwtUtil.ensureAdminFromContext();
        Groups groups = groupsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with ID: " + id));

        groups.setActive(!groups.isActive());
        Groups updated = groupsRepository.save(groups);

        // Activate or Inactive all Clients registered with this group
        /*if (client.getActive()) {
            credentialsRepository.activateAllByClientId(updated.getId());
        } else {
            credentialsRepository.deactivateAllByClientId(updated.getId());
        }*/

        String status = updated.isActive() ? "activated" : "deactivated";

        return Response.<GroupsResponse>builder()
                .data(toResponse(updated))
                .httpStatusCode(HttpStatus.OK.value())
                .message("Client " + status + " successfully")
                .build();
    }

    private GroupsResponse toResponse(Groups groups) {

        return GroupsResponse.builder()
                .id(groups.getId())
                .name(groups.getName())
                .alias(groups.getAlias())
                .representativeName(groups.getRepresentativeName())
                .mobileNumber(groups.getMobileNumber())
                .email(groups.getEmail())
                .active(groups.isActive())
                .build();
    }

}
