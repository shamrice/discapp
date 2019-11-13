package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.Thread;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {

    List<Thread> findByApplicationIdAndDeleted(Long applicationId, Boolean deleted);
    List<Thread> findByApplicationIdAndDeletedAndIsApproved(Long applicationId, Boolean deleted, Boolean isApproved);
    List<Thread> findByApplicationIdAndParentIdAndDeleted(Long applicationId, Long parentId, Boolean deleted);
    List<Thread> findByApplicationIdAndParentIdAndDeletedAndIsApproved(Long applicationId, Long parentId, Boolean deleted, Boolean isApproved);
    List<Thread> findByApplicationIdAndParentIdAndCreateDtBetween(Long applicationId, Long parentId, Date createDtStart,Date createDtEnd);

   // List<Thread> findByApplicationIdAndDeletedOrderByCreateDtDesc(Long applicationId, Boolean deleted, Pageable pageable);
    List<Thread> findByApplicationIdAndDeletedAndIsApprovedOrderByCreateDtDesc(Long applicationId, Boolean deleted, Boolean isApproved, Pageable pageable);

    //List<Thread> findByApplicationIdAndParentIdAndDeletedOrderByCreateDtDesc(Long applicationId, Long parentId, Boolean deleted, Pageable pageable);
    List<Thread> findByApplicationIdAndParentIdAndDeletedAndIsApprovedOrderByCreateDtDesc(Long applicationId, Long parentId, Boolean deleted, Boolean isApproved, Pageable pageable);

    //List<Thread> findByApplicationIdAndDeletedAndSubjectContainingIgnoreCaseOrderByCreateDtDesc(Long applicationId, Boolean deleted, String subject);
    List<Thread> findByApplicationIdAndDeletedAndIsApprovedAndSubjectContainingIgnoreCaseOrderByCreateDtDesc(Long applicationId, Boolean deleted, Boolean isApproved, String subject);

    //List<Thread> findByApplicationIdAndDeletedAndSubmitterContainingIgnoreCase(Long applicationId, Boolean deleted, String submitter);
    List<Thread> findByApplicationIdAndDeletedAndIsApprovedAndSubmitterContainingIgnoreCase(Long applicationId, Boolean deleted, Boolean isApproved, String submitter);
    //List<Thread> findByApplicationIdAndDeletedAndIpAddressContainingIgnoreCase(Long applicationId, Boolean deleted, String ipAddress);
    List<Thread> findByApplicationIdAndDeletedAndIsApprovedAndIpAddressContainingIgnoreCase(Long applicationId, Boolean deleted, Boolean isApproved, String ipAddress);
    //List<Thread> findByApplicationIdAndDeletedAndEmailContainingIgnoreCase(Long applicationId, Boolean deleted, String email);
    List<Thread> findByApplicationIdAndDeletedAndIsApprovedAndEmailContainingIgnoreCase(Long applicationId, Boolean deleted, Boolean isApproved, String email);

    List<Thread> findByApplicationIdAndDeletedAndIsApprovedOrderByCreateDtDesc(Long applicationId, Boolean deleted, Boolean isApproved);

    List<Thread> findByDeletedOrderByCreateDtDesc(Boolean deleted);

    Thread getOneByApplicationIdAndId(Long applicationId, Long id);

    Thread findTopByApplicationIdAndIpAddressOrderByCreateDtDesc(Long applicationId, String ipAddress);


    //todo add approved

    long countByApplicationIdAndDeleted(Long applicationId, Boolean deleted);
}
