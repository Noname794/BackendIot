package com.smartlight.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {
    private Long id;
    private String name;
    private Integer size;
    private String sizeUnit;
    private String image;
    private Long spaceId;
    private List<DeviceDTO> devices;
}
