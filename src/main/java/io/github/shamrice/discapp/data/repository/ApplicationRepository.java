package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByOwnerIdAndDeleted(Long ownerId, Boolean deleted);

    List<Application> findByNameContainingIgnoreCaseAndDeletedAndEnabledAndSearchableOrderByNameAsc(String applicationName, Boolean deleted, Boolean enabled, Boolean searchable);

}
