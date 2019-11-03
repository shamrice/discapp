package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository(value = "editor_permission")
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    List<UserPermission> findByApplicationId(Long applicationId);
    List<UserPermission> findByDiscAppUserIdAndIsActive(Long discAppUserId, Boolean isActive);
    UserPermission findOneByApplicationIdAndDiscAppUserIdAndIsActive(Long applicationId, Long discAppUserId, Boolean isActive);
}
