package com.ttwreis.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invigilators")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Invigilator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 60)
    private String designation;

    @Column(length = 15)
    private String mobile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_center_id")
    private ExamCenter examCenter;

    private boolean active = true;
}
