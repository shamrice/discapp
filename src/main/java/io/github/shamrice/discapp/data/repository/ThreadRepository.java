package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.Thread;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {

    List<Thread> findByApplicationIdAndDeleted(Long applicationId, Boolean deleted);
    List<Thread> findByApplicationIdAndParentIdAndDeleted(Long applicationId, Long parentId, Boolean deleted);
    List<Thread> findByApplicationIdAndParentIdAndCreateDtBetween(Long applicationId, Long parentId, Date createDtStart,Date createDtEnd);

    List<Thread> findByApplicationIdAndParentIdAndDeletedOrderByCreateDtDesc(Long applicationId, Long parentId, Boolean deleted, Pageable pageable);

    List<Thread> findByApplicationIdAndDeletedAndSubjectContainingIgnoreCaseOrderByCreateDtDesc(Long applicationId, Boolean deleted, String subject);

    long countByApplicationIdAndDeleted(Long applicationId, Boolean deleted);
}
