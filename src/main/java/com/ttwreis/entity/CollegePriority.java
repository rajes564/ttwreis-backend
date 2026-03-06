package com.ttwreis.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "college_priorities",
       uniqueConstraints = @UniqueConstraint(columnNames = {"application_id", "priority_order"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CollegePriority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    /** 1-based rank (1 = highest preference) */
    @Column(nullable = false)
    private Integer priorityOrder;

    /** Institute / college name or ID */
    @Column(nullable = false, length = 200)
    private String collegeName;

    /** Optional FK to institutes table */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institute_id")
    private Institute institute;
}
