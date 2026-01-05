package com.smartlight.service;

import com.smartlight.dto.*;
import com.smartlight.entity.*;
import com.smartlight.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpaceService {
    private final SpaceRepository spaceRepository;
    private final UserService userService;

    public List<SpaceDTO> getSpacesByUser(String email) {
        User user = userService.getUserEntityByEmail(email);
        return spaceRepository.findByUserId(user.getId()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public SpaceDTO getSpaceById(Long id) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Space not found"));
        return toDTO(space);
    }

    @Transactional
    public SpaceDTO createSpace(String email, SpaceDTO dto) {
        User user = userService.getUserEntityByEmail(email);
        
        Space space = Space.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .image(dto.getImage())
                .user(user)
                .build();

        spaceRepository.save(space);
        return toDTO(space);
    }

    @Transactional
    public SpaceDTO updateSpace(Long id, SpaceDTO dto) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Space not found"));

        space.setName(dto.getName());
        space.setAddress(dto.getAddress());
        if (dto.getImage() != null) {
            space.setImage(dto.getImage());
        }

        spaceRepository.save(space);
        return toDTO(space);
    }

    @Transactional
    public void deleteSpace(Long id) {
        spaceRepository.deleteById(id);
    }

    private SpaceDTO toDTO(Space space) {
        List<RoomDTO> rooms = space.getRooms() != null 
                ? space.getRooms().stream().map(this::toRoomDTO).collect(Collectors.toList())
                : List.of();

        List<DeviceDTO> devices = space.getDevices() != null
                ? space.getDevices().stream().map(this::toDeviceDTO).collect(Collectors.toList())
                : List.of();

        return SpaceDTO.builder()
                .id(space.getId())
                .name(space.getName())
                .address(space.getAddress())
                .image(space.getImage())
                .rooms(rooms)
                .devices(devices)
                .roomCount(rooms.size())
                .deviceCount(devices.size())
                .build();
    }

    private RoomDTO toRoomDTO(Room room) {
        return RoomDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .size(room.getSize())
                .sizeUnit(room.getSizeUnit())
                .image(room.getImage())
                .spaceId(room.getSpace().getId())
                .build();
    }

    private DeviceDTO toDeviceDTO(Device device) {
        return DeviceDTO.builder()
                .id(device.getId())
                .name(device.getName())
                .deviceType(device.getDeviceType())
                .image(device.getImage())
                .isOn(device.getIsOn())
                .mqttTopic(device.getMqttTopic())
                .spaceId(device.getSpace().getId())
                .roomId(device.getRoom() != null ? device.getRoom().getId() : null)
                .roomName(device.getRoom() != null ? device.getRoom().getName() : null)
                .build();
    }
}
