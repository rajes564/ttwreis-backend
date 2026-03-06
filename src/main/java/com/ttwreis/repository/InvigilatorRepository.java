package com.ttwreis.repository;

import com.ttwreis.entity.Invigilator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InvigilatorRepository extends JpaRepository<Invigilator, Long> {

    @Query("""
        SELECT i FROM Invigilator i LEFT JOIN i.examCenter ec
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(i.name) LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(i.designation) LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(i.mobile) LIKE LOWER(CONCAT('%',:search,'%')))
        ORDER BY i.name
        """)
    Page<Invigilator> searchInvigilators(@Param("search") String search, Pageable pageable);
}
