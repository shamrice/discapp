package io.github.shamrice.discapp.data.repository;


import io.github.shamrice.discapp.data.model.ThreadActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadActivityRepository extends JpaRepository<ThreadActivity, Long> {

    Optional<ThreadActivity> findByThreadId(Long threadId);
    List<ThreadActivity> findByApplicationIdAndThreadDeletedAndThreadIsApprovedOrderByModDtDesc(Long applicationId, Boolean deleted, Boolean isApproved, Pageable pageable);
}
