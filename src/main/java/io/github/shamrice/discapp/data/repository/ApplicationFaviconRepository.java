package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.ApplicationFavicon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationFaviconRepository extends JpaRepository<ApplicationFavicon, Long> {

    Optional<ApplicationFavicon> findOneByApplicationId(Long applicationId);

}
