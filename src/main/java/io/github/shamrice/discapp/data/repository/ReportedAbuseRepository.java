package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.ReportedAbuse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportedAbuseRepository extends JpaRepository<ReportedAbuse, Long> {
}
