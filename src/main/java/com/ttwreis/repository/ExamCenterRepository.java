package com.ttwreis.repository;

import com.ttwreis.entity.ExamCenter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamCenterRepository extends JpaRepository<ExamCenter, Long> {

    @Query("""
        SELECT e FROM ExamCenter e
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(e.name) LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(e.district) LIKE LOWER(CONCAT('%',:search,'%')))
        ORDER BY e.name
        """)
    Page<ExamCenter> searchCenters(@Param("search") String search, Pageable pageable);
}
