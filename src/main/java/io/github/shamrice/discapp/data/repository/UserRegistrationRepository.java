package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.UserRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRegistrationRepository extends JpaRepository<UserRegistration, Long> {

    UserRegistration findOneByEmailAndKey(String email, String key);

    UserRegistration findOneByEmail(String email);
}
