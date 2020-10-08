package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.ApplicationReportCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationReportCodeRepository extends JpaRepository<ApplicationReportCode, Long> {

    Optional<ApplicationReportCode> findByApplicationIdAndEmailAndCode(Long applicationId, String email, String code);
}
