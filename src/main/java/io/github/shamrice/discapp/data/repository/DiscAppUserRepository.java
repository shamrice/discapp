package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository(value = "discapp_user")
public interface DiscAppUserRepository extends JpaRepository<DiscAppUser, Long> {

    @Query(value = "SELECT u FROM discapp_user u WHERE lower(u.username) = lower(:username)")
    DiscAppUser findByUsername(@Param("username") String username);

}
