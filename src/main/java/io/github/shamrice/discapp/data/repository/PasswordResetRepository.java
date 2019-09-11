package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    PasswordReset findOneByEmailAndKey(String email, String key);

    @Transactional
    void deleteByEmail(String email);
}
