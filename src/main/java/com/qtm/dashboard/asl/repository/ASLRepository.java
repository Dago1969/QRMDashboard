package com.qtm.dashboard.asl.repository;

import com.qtm.dashboard.asl.entity.ASLEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ASLRepository extends JpaRepository<ASLEntity, Long> {
}
