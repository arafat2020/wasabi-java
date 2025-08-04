package com.wasabi.wasabi.src.fileupload.repository;

import com.wasabi.wasabi.src.fileupload.model.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, UUID> {
    
    Optional<FileUpload> findByFileName(String fileName);
    
    List<FileUpload> findByUploadedBy(String uploadedBy);
    
    List<FileUpload> findByContentTypeStartingWith(String contentTypePrefix);
    
    @Query("SELECT f FROM FileUpload f WHERE f.uploadedBy = :uploadedBy AND f.contentType LIKE :contentType%")
    List<FileUpload> findByUploadedByAndContentType(@Param("uploadedBy") String uploadedBy, 
                                                   @Param("contentType") String contentType);
    
    @Query("SELECT f FROM FileUpload f WHERE f.isPublic = true")
    List<FileUpload> findAllPublicFiles();
    
    void deleteByFileName(String fileName);
}
