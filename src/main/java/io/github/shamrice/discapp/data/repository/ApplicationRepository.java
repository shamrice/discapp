package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
