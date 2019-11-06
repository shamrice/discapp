package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.SiteUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteUpdateLogRepository extends JpaRepository<SiteUpdateLog, Long> {

    SiteUpdateLog findTopByAndEnabledOrderByCreateDtDesc(Boolean enabled);
}
