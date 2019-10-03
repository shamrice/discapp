package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.ImportData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImportDataRepository extends JpaRepository<ImportData, Long> {

    Optional<ImportData> findByApplicationId(Long applicationId);
}
