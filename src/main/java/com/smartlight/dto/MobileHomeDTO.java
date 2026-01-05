package com.smartlight.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MobileHomeDTO {
    private Long id;
    private String name;
    private String country;
    private String address;
    private List<String> rooms;
    private Boolean isActive;
    private Long userId;
}
