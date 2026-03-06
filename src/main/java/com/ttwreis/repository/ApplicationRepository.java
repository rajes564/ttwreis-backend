package com.ttwreis.repository;

import com.ttwreis.entity.Application;
import com.ttwreis.entity.Application.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByUserRegistrationNumber(String registrationNumber);
    Optional<Application> findByUserId(Long userId);
    Optional<Application> findByApplicationNumber(String applicationNumber);

    long countByStatus(ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a JOIN a.payment p WHERE p.status = 'PAID'")
    long countPaid();

    @Query("SELECT a.community, COUNT(a) FROM Application a WHERE a.community IS NOT NULL GROUP BY a.community")
    List<Object[]> countGroupedByCommunity();

    @Query("""
        SELECT a FROM Application a LEFT JOIN a.user u LEFT JOIN a.payment p
        WHERE (:status IS NULL OR a.status = :status)
          AND (:search IS NULL OR :search = ''
               OR LOWER(a.applicationNumber) LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(u.candidateName)     LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(a.community)         LIKE LOWER(CONCAT('%',:search,'%')))
        ORDER BY a.createdAt DESC
        """)
    Page<Application> searchApplications(
        @Param("status") ApplicationStatus status,
        @Param("search") String search,
        Pageable pageable
    );
}
