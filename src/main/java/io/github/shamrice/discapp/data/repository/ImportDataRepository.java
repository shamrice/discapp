package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.ImportData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImportDataRepository extends JpaRepository<ImportData, Long> {

    Optional<ImportData> findByApplicationId(Long applicationId);
}
