package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByOwnerId(Long ownerId);
}
