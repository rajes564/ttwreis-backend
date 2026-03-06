package com.ttwreis.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_centers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ExamCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    private String address;
    private String district;

    private Integer noOfRooms;
    private Integer roomCapacity;
    private boolean active = true;

    public int getTotalCapacity() {
        int rooms    = noOfRooms    != null ? noOfRooms    : 0;
        int capacity = roomCapacity != null ? roomCapacity : 0;
        return rooms * capacity;
    }
}
