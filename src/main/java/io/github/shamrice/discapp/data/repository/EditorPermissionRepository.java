package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.EditorPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EditorPermissionRepository extends JpaRepository<EditorPermission, Long> {

    List<EditorPermission> findByApplicationId(Long applicationId);
}
