package com.qtm.dashboard.user.repository;

import com.qtm.dashboard.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository utenti centralizzati.
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    List<UserEntity> findAllByRole_Id(String roleId);

    default List<UserEntity> findAllByRoleId(String roleId) {
        return findAllByRole_Id(roleId);
    }

    Optional<UserEntity> findByUsernameIgnoreCase(String username);

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    Optional<UserEntity> findByPasswordResetToken(String passwordResetToken);
}
