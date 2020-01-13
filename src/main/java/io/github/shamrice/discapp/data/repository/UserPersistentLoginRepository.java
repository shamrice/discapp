package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.UserPersistentLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPersistentLoginRepository extends JpaRepository<UserPersistentLogin, String> {

    void deleteByUsername(String username);
}
