package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.Thread;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {

    List<Thread> findByApplicationId(Long applicationId);
    List<Thread> findByApplicationIdAndParentId(Long applicationId, Long parentId);
    List<Thread> findByApplicationIdAndParentIdAndCreateDtBetween(Long applicationId, Long parentId, Date createDtStart,Date createDtEnd);

    List<Thread> findByApplicationIdAndParentIdOrderByCreateDtDesc(Long applicationId, Long parentId, Pageable pageable);

    List<Thread> findByApplicationIdAndSubjectContainingIgnoreCaseOrderByCreateDtDesc(Long applicationId, String subject);

    long countByApplicationId(Long applicationId);
}
