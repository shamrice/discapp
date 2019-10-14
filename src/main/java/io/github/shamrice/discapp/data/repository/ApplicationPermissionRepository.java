package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.ApplicationPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationPermissionRepository extends JpaRepository<ApplicationPermission, Long> {

    Optional<ApplicationPermission> findOneByApplicationId(long applicationId);
}
