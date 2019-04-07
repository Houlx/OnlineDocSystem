package com.hou.gradproj.docmanagesys.repository;

import com.hou.gradproj.docmanagesys.model.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByName(String name);

    Optional<File> findById(Long id);

    Page<File> findByCreatedBy(Long id, Pageable pageable);

    List<File> findByIdIn(List<Long> ids);
}
