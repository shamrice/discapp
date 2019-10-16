package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.ReportedAbuse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportedAbuseRepository extends JpaRepository<ReportedAbuse, Long> {

    List<ReportedAbuse> findByApplicationId(Long applicationId);
}
