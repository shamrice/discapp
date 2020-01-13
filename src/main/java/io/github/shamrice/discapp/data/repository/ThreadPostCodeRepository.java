package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.ThreadPostCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThreadPostCodeRepository extends JpaRepository<ThreadPostCode, String> {
}
