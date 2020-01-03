package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.UserConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserConfigurationRepository extends JpaRepository<UserConfiguration, Long> {

    UserConfiguration findOneByDiscappUserIdAndName(Long discappUserId, String name);

}
