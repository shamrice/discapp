package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.ApplicationIpBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationIpBlockRepository extends JpaRepository<ApplicationIpBlock, Long> {

    List<ApplicationIpBlock> findByApplicationId(Long applicationId);
}
