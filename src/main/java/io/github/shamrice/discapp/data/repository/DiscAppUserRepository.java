package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository(value = "discapp_user")
public interface DiscAppUserRepository extends JpaRepository<DiscAppUser, Long> {

    DiscAppUser findByUsername(String username);

}
