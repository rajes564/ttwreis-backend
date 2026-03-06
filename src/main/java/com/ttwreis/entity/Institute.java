package com.ttwreis.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "institutes")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Institute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(unique = true, length = 30)
    private String code;

    private String district;
    private String mandal;
    private String address;

    /** Boys / Girls / Co-Ed */
    @Column(length = 10)
    private String type;

    private Integer totalSeats;
    private Integer filledSeats = 0;
    private boolean active = true;

    public int getVacantSeats() {
        int total  = totalSeats  != null ? totalSeats  : 0;
        int filled = filledSeats != null ? filledSeats : 0;
        return Math.max(0, total - filled);
    }
}
