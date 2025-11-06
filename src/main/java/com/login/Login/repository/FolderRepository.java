package com.login.Login.repository;

import com.login.Login.dto.folder.FolderResponse;
import com.login.Login.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder,Long> {
    List<Folder> findByParentIdAndActiveTrue(Long parentId);
    List<Folder> findByParentIdAndActiveFalse(Long parentId);
    Optional<Folder> findByNameAndActiveTrue(String name);
    Optional<Folder> findByPathAndActiveTrue(String path);

    Optional<Folder> findByPathAndActiveFalse(String path);
    /*@Query("""
    SELECT f FROM Folders f
    WHERE f.folders.userId = :userId
    ORDER BY path asc
    """)
    List<Folder> searchAllFolders(@Param("userId") Long userId);*/
    @Query("SELECT a FROM Folder a WHERE a.user.id = :userId AND (a.path NOT IN (:pathString) AND a.active = true) ORDER BY a.path ASC")
    List<Folder> findByEntityBIdCustomQuery(
            @Param("userId") Long userId,@Param("pathString") String path
    );
}
