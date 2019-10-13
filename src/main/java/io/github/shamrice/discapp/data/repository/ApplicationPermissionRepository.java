package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.ApplicationPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationPermissionRepository extends JpaRepository<ApplicationPermission, Long> {

    Optional<ApplicationPermission> findOneByApplicationId(long applicationId);
}
