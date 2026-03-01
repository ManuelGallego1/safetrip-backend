package com.safetrip.backend.infrastructure.persistence.repository;

import com.safetrip.backend.domain.model.enums.DiscountType;
import com.safetrip.backend.infrastructure.persistence.entity.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountJpaRepository extends JpaRepository<DiscountEntity, Long> {

    Optional<DiscountEntity> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndDiscountIdNot(String name, Long discountId);

    List<DiscountEntity> findAllByActiveTrue();

    @Query("""
        SELECT d FROM DiscountEntity d
        WHERE d.type = :type
        ORDER BY d.name ASC
        """)
    List<DiscountEntity> findByType(@Param("type") DiscountType type);
}