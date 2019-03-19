package com.hou.gradproj.docmanagesys.repository;

import com.hou.gradproj.docmanagesys.model.Role;
import com.hou.gradproj.docmanagesys.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName roleName);
}
