package com.ttwreis.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDto {
    private long   totalApplications;
    private long   paidApplications;
    private long   pendingApplications;
    private double totalAmountReceived;
    private long   hallTicketsGenerated;
    private long   appeared;
    private long   notAppeared;
    private List<Map<String, Object>> categoryBreakdown;
    private List<Map<String, Object>> collegeWise;
}
