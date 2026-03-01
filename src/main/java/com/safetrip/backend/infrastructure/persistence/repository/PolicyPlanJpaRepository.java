package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.infrastructure.persistence.entity.PolicyPlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyPlanJpaRepository extends JpaRepository<PolicyPlanEntity, Long> {

    @Query("""
        SELECT pp FROM PolicyPlanEntity pp
        WHERE pp.policyType.policyTypeId = :policyTypeId
        ORDER BY pp.pax ASC
    """)
    List<PolicyPlanEntity> findByPolicyTypeId(@Param("policyTypeId") Long policyTypeId);

    @Query("""
        SELECT pp FROM PolicyPlanEntity pp
        WHERE pp.policyType.policyTypeId = :policyTypeId
        AND pp.active = :active
        ORDER BY pp.pax ASC
    """)
    List<PolicyPlanEntity> findByPolicyTypeIdAndActive(
            @Param("policyTypeId") Long policyTypeId,
            @Param("active") Boolean active);

    @Query("""
        SELECT pp FROM PolicyPlanEntity pp
        WHERE pp.pax = :pax
        AND pp.active = true
        ORDER BY pp.policyType.policyTypeId ASC
    """)
    List<PolicyPlanEntity> findByPax(@Param("pax") Integer pax);

    @Query("""
        SELECT pp FROM PolicyPlanEntity pp
        WHERE pp.policyType.policyTypeId = :policyTypeId
        AND pp.pax = :pax
    """)
    List<PolicyPlanEntity> findByPolicyTypeIdAndPax(
            @Param("policyTypeId") Long policyTypeId,
            @Param("pax") Integer pax);

    @Query("""
        SELECT pp FROM PolicyPlanEntity pp
        WHERE pp.active = true
        ORDER BY pp.policyType.policyTypeId, pp.pax ASC
    """)
    List<PolicyPlanEntity> findAllActive();

    @Query("""
        SELECT pp FROM PolicyPlanEntity pp
        WHERE pp.popular = true
        AND pp.active = true
        ORDER BY pp.policyType.policyTypeId, pp.pax ASC
    """)
    List<PolicyPlanEntity> findAllPopular();

    Page<PolicyPlanEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<PolicyPlanEntity> findByActiveOrderByCreatedAtDesc(Boolean active, Pageable pageable);
}