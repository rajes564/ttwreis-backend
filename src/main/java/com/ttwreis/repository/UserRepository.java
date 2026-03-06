package com.ttwreis.repository;

import com.ttwreis.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByRegistrationNumber(String registrationNumber);
    Optional<User> findByEmail(String email);
    Optional<User> findByRegistrationNumberOrEmail(String registrationNumber, String email);

    boolean existsByAadhaarNumber(String aadhaarNumber);
    boolean existsByEmail(String email);
    boolean existsByMobileNumber(String mobileNumber);
    boolean existsByRegistrationNumber(String registrationNumber);

    List<User> findByRole(User.Role role);

    @Query("""
        SELECT u FROM User u
        WHERE u.role = 'CANDIDATE'
          AND (:search IS NULL OR :search = ''
               OR LOWER(u.registrationNumber) LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(u.candidateName)      LIKE LOWER(CONCAT('%',:search,'%'))
               OR u.mobileNumber LIKE CONCAT('%',:search,'%'))
        ORDER BY u.id DESC
        """)
    Page<User> searchCandidates(@Param("search") String search, Pageable pageable);
}
