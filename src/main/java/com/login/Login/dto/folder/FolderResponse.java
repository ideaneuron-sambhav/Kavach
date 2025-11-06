package com.login.Login.dto.folder;

import com.login.Login.entity.Folder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class FolderResponse {
    private Long folderId;
    private String name;
    private String type;
    private Long parentId;
    private String parentPath;
    private Long userId;
    private String path;
    private LocalDateTime createdAt;
    public static FolderResponse from(Folder folder) {
        String str = String.valueOf(folder.getUser().getId());
        return new FolderResponse(folder.getId(), folder.getName(), folder.getType().name(), folder.getParent().getId(), folder.getParent().getPath().substring(str.length()+1), folder.getUser().getId(), folder.getPath().substring(str.length()+1), folder.getCreatedAt());
    }
}
