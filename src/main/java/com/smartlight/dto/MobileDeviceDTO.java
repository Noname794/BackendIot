package com.smartlight.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MobileDeviceDTO {
    private Long id;
    private String deviceCode;
    private String name;
    private String type;
    private String topic;
    private Boolean status;
    private Integer brightness;
    private String color;
    private Long userId;
    private String roomName;
}
