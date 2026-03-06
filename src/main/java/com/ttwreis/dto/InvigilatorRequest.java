package com.ttwreis.dto;

import lombok.Data;

@Data
public class InvigilatorRequest {
    private String name;
    private String designation;
    private String mobile;
    private Long   examCenterId;
}
