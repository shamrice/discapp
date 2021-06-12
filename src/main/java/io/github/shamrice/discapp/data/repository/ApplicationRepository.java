package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.Application;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByOwnerIdAndDeleted(Long ownerId, Boolean deleted);
    Optional<Application> findOneByIdAndOwnerIdAndDeleted(Long appId, Long ownerId, Boolean deleted);

    List<Application> findByNameContainingIgnoreCaseAndDeletedAndEnabledAndSearchableOrderByIdAsc(String applicationName, Boolean deleted, Boolean enabled, Boolean searchable, Pageable pageable);
    long countByNameContainingIgnoreCaseAndDeletedAndEnabledAndSearchable(String applicationName, Boolean deleted, Boolean enabled, Boolean searchable);

}
