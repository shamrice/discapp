package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.EditorPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository(value = "editor_permission")
public interface EditorPermissionRepository extends JpaRepository<EditorPermission, Long> {

    List<EditorPermission> findByApplicationId(Long applicationId);
    List<EditorPermission> findByDiscAppUserId(Long discAppUserId);
    EditorPermission findOneByApplicationIdAndDiscAppUserIdAndIsActive(Long applicationId, Long discAppUserId, Boolean isActive);
}
