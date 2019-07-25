package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.DiscappUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Repository;

@Repository(value = "discapp_user")
public interface DiscappUserRepository extends JpaRepository<DiscappUser, Long> {

    DiscappUser findByUsername(String username);

}
