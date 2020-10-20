package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.DiscAppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository(value = "discapp_user")
public interface DiscAppUserRepository extends JpaRepository<DiscAppUser, Long> {

    @Query(value = "SELECT u FROM discapp_user u WHERE lower(u.username) = lower(:username)")
    DiscAppUser findByUsername(@Param("username") String username);


    @Query(value = "SELECT u FROM discapp_user u WHERE lower(u.email) = lower(:email)")
    DiscAppUser findByEmail(@Param("email") String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE discapp_user SET username = :username, show_email = :showEmail, mod_dt = :modDt WHERE id = :id")
    int updateDiscAppUser(Long id, String username, Boolean showEmail, Date modDt);

    @Modifying
    @Transactional
    @Query(value = "UPDATE discapp_user SET last_login_date = :lastLoginDate, password_fail_count = :passwordFailCount WHERE id = :id")
    int updateDiscAppUserLastLoginDateAndPasswordFailCountById(Long id, Date lastLoginDate, Integer passwordFailCount);

    @Modifying
    @Transactional
    @Query(value = "UPDATE discapp_user " +
            "SET password_fail_count = :passwordFailCount, " +
            "last_password_fail_date = :lastPasswordFailDate, " +
            "locked_until_date = :lockedUntilDate " +
            "WHERE id = :id")
    int updateDiscAppUserPasswordFailCountAndLastPasswordFailDateAndLockedUntilDateById(Long id, Integer passwordFailCount, Date lastPasswordFailDate, Date lockedUntilDate);

    @Modifying
    @Transactional
    @Query(value = "UPDATE discapp_user " +
            "SET password_fail_count = :passwordFailCount, " +
            "last_password_fail_date = :lastPasswordFailDate " +
            "WHERE id = :id")
    int updateDiscAppUserPasswordFailCountAndLastPasswordFailDateById(Long id, Integer passwordFailCount, Date lastPasswordFailDate);

    @Modifying
    @Transactional
    @Query(value = "UPDATE discapp_user SET owner_id = :ownerId, is_admin = :isAdmin, mod_dt = :modDt WHERE id = :id")
    int updateDiscAppUserOwnerInfo(Long id, Long ownerId, Boolean isAdmin, Date modDt);

    @Modifying
    @Transactional
    @Query(value = "UPDATE discapp_user SET enabled = :enabled, mod_dt = :modDt WHERE id = :id")
    int updateDiscAppUserEnabled(Long id, Boolean enabled, Date modDt);

    List<DiscAppUser> findByUsernameContainingIgnoreCaseAndIsUserAccount(String username, boolean isUserAccount);

    List<DiscAppUser> findByEmailContainingIgnoreCaseAndIsUserAccount(String email, boolean isUserAccount);

    List<DiscAppUser> findByOwnerId(Long ownerId);
}
