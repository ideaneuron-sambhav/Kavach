package com.login.Login.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "folders")
@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private FolderType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private Folder parent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;


    private String path;

    private LocalDateTime createdAt;

    private boolean active = true;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

/*    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Folder> subfolders;*/

    public enum FolderType{
        FOLDER, FILE
    }

}
