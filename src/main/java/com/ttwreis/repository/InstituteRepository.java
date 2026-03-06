package com.ttwreis.repository;

import com.ttwreis.entity.Institute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InstituteRepository extends JpaRepository<Institute, Long> {
    List<Institute> findByActiveTrue();

    @Query("""
        SELECT i FROM Institute i
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(i.name) LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(i.district) LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(i.code) LIKE LOWER(CONCAT('%',:search,'%')))
        ORDER BY i.name
        """)
    Page<Institute> searchInstitutes(@Param("search") String search, Pageable pageable);
}
