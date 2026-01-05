package com.smartlight.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDTO {
    private Long id;
    private String name;
    private String deviceType;
    private String image;
    private Boolean isOn;
    private String mqttTopic;
    private Long spaceId;
    private Long roomId;
    private String roomName;
}
