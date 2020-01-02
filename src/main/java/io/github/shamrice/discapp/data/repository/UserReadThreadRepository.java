package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.UserReadThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserReadThreadRepository extends JpaRepository<UserReadThread, Long> {

    UserReadThread findOneByApplicationIdAndDiscappUserId(Long applicationId, Long discappUserId);

    List<UserReadThread> findAllByDiscappUserId(Long discappUserId);
}
