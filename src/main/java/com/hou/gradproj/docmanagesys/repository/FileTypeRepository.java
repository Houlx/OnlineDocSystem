package com.hou.gradproj.docmanagesys.repository;

import com.hou.gradproj.docmanagesys.model.FileType;
import com.hou.gradproj.docmanagesys.model.FileTypeName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileTypeRepository extends JpaRepository<FileType, Long> {
    Optional<FileType> findByName(FileTypeName fileTypeName);

    Optional<FileType> findById(Long id);
}
