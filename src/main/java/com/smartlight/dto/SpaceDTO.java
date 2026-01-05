package com.smartlight.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaceDTO {
    private Long id;
    private String name;
    private String address;
    private String image;
    private List<RoomDTO> rooms;
    private List<DeviceDTO> devices;
    private int roomCount;
    private int deviceCount;
}
