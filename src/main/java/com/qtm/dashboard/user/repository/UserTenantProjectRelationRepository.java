package com.qtm.dashboard.user.repository;

import com.qtm.dashboard.user.entity.UserTenantProjectRelation;
import com.qtm.dashboard.user.entity.UserTenantProjectRelationId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository per la relazione User-Tenant-Project.
 */
@Repository
public interface UserTenantProjectRelationRepository extends JpaRepository<UserTenantProjectRelation, UserTenantProjectRelationId> {
    // Versioni standard (deprecated, non usare per DTO)
    List<UserTenantProjectRelation> findByUserId(Long userId);
    List<UserTenantProjectRelation> findByTenantId(Long tenantId);
    List<UserTenantProjectRelation> findByProjectId(Long projectId);

    // Versioni con fetch join per evitare LazyInitializationException
    @org.springframework.data.jpa.repository.Query("""
        select r from UserTenantProjectRelation r
        join fetch r.tenant
        join fetch r.user
        left join fetch r.project
        where r.user.id = :userId
    """)
    List<UserTenantProjectRelation> findByUserIdWithFetch(Long userId);

    @org.springframework.data.jpa.repository.Query("""
        select r from UserTenantProjectRelation r
        join fetch r.tenant
        join fetch r.user
        left join fetch r.project
        where r.user.id = :userId
          and r.tenant.id = :tenantId
    """)
    List<UserTenantProjectRelation> findByUserIdAndTenantIdWithFetch(Long userId, Long tenantId);

    @org.springframework.data.jpa.repository.Query("""
        select r from UserTenantProjectRelation r
        join fetch r.tenant
        join fetch r.user
        left join fetch r.project
        where r.tenant.id = :tenantId
    """)
    List<UserTenantProjectRelation> findByTenantIdWithFetch(Long tenantId);

    @org.springframework.data.jpa.repository.Query("""
        select r from UserTenantProjectRelation r
        join fetch r.tenant
        join fetch r.user
        left join fetch r.project
        where r.project.id = :projectId
    """)
    List<UserTenantProjectRelation> findByProjectIdWithFetch(Long projectId);

    @org.springframework.data.jpa.repository.Query("""
        select r from UserTenantProjectRelation r
        join fetch r.tenant
        join fetch r.user
        join fetch r.project
        where r.user.id = :userId
          and r.tenant.id = :tenantId
          and r.project.id = :projectId
    """)
    Optional<UserTenantProjectRelation> findByUserIdAndTenantIdAndProjectIdWithFetch(Long userId, Long tenantId, Long projectId);
}
