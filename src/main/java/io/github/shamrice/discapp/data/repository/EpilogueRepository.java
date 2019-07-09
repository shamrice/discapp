package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.Epilogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EpilogueRepository extends JpaRepository<Epilogue, Long> {

    Epilogue findOneByApplicationId(Long applicationId);
}
