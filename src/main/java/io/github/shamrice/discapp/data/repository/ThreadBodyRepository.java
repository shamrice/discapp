package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.ThreadBody;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThreadBodyRepository extends JpaRepository<ThreadBody, Long> {

    ThreadBody findByThreadId(Long threadId);
}
