package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

    Configuration findOneByApplicationIdAndName(Long applicationId, String name);

    Configuration findOneByName(String name);
}
