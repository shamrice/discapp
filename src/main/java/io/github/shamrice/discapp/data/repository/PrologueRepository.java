package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.Prologue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrologueRepository extends JpaRepository<Prologue, Long> {

    Prologue findOneByApplicationId(Long applicationId);
}
