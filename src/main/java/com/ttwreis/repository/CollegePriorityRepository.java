package com.ttwreis.repository;

import com.ttwreis.entity.CollegePriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CollegePriorityRepository extends JpaRepository<CollegePriority, Long> {
    List<CollegePriority> findByApplicationIdOrderByPriorityOrder(Long applicationId);
    void deleteByApplicationId(Long applicationId);
}
