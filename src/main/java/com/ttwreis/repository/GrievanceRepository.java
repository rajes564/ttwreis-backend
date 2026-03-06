package com.ttwreis.repository;

import com.ttwreis.entity.Grievance;
import com.ttwreis.entity.Grievance.GrievanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrievanceRepository extends JpaRepository<Grievance, Long> {

    Optional<Grievance> findByGrievanceNumber(String grievanceNumber);

    Optional<Grievance> findByMobileNumberAndGrievanceNumber(String mobile, String grievanceNumber);

    @Query("""
        SELECT g FROM Grievance g
        WHERE (:status IS NULL OR g.status = :status)
          AND (:search IS NULL OR :search = ''
               OR LOWER(g.grievanceNumber)   LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(g.applicantName)     LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(g.mobileNumber)      LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(g.registrationNumber)LIKE LOWER(CONCAT('%',:search,'%')))
        ORDER BY g.createdAt DESC
        """)
    Page<Grievance> searchGrievances(
        @Param("status") GrievanceStatus status,
        @Param("search") String search,
        Pageable pageable
    );

    long countByStatus(GrievanceStatus status);

    // For applicant dashboard — find by registration number
    Page<Grievance> findByRegistrationNumberOrderByCreatedAtDesc(String regNo, Pageable pageable);
}