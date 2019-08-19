package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.StatsUniqueIps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatsUniqueIpsRepository extends JpaRepository<StatsUniqueIps, Long> {

    List<StatsUniqueIps> findByStatsId(long statsId);
}
