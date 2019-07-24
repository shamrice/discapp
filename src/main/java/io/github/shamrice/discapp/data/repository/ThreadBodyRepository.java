package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.ThreadBody;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreadBodyRepository extends JpaRepository<ThreadBody, Long> {

    ThreadBody findByThreadId(Long threadId);
    List<ThreadBody> findByApplicationIdAndBodyContainingIgnoreCaseOrderByCreateDtDesc(Long applicationId, String body);
}
