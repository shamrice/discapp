package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
}
