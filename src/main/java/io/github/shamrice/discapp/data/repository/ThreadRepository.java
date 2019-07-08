package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, Long> {

    List<Thread> findByApplicationId(Long applicationId);
    List<Thread> findByApplicationIdAndParentId(Long applicationId, Long parentId);
}
