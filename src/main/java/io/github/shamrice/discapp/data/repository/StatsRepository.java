package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.Stats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Stats, Long> {

    Stats findOneByApplicationIdAndStatDate(long applicationId, String statDate);
    List<Stats> findByApplicationIdAndStatDateOrderByCreateDtDesc(long applicationId, String statDate);

    List<Stats> findByApplicationIdOrderByCreateDtDesc(long applicationId, Pageable pageable);
}
