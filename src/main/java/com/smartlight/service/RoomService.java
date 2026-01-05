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
public class RoomService {
    private final RoomRepository roomRepository;
    private final SpaceRepository spaceRepository;

    public List<RoomDTO> getRoomsBySpace(Long spaceId) {
        return roomRepository.findBySpaceId(spaceId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return toDTO(room);
    }

    @Transactional
    public RoomDTO createRoom(Long spaceId, RoomDTO dto) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new RuntimeException("Space not found"));

        Room room = Room.builder()
                .name(dto.getName())
                .size(dto.getSize())
                .sizeUnit(dto.getSizeUnit())
                .image(dto.getImage())
                .space(space)
                .build();

        roomRepository.save(room);
        return toDTO(room);
    }

    @Transactional
    public RoomDTO updateRoom(Long id, RoomDTO dto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setName(dto.getName());
        room.setSize(dto.getSize());
        room.setSizeUnit(dto.getSizeUnit());
        if (dto.getImage() != null) {
            room.setImage(dto.getImage());
        }

        roomRepository.save(room);
        return toDTO(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    private RoomDTO toDTO(Room room) {
        List<DeviceDTO> devices = room.getDevices() != null
                ? room.getDevices().stream().map(this::toDeviceDTO).collect(Collectors.toList())
                : List.of();

        return RoomDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .size(room.getSize())
                .sizeUnit(room.getSizeUnit())
                .image(room.getImage())
                .spaceId(room.getSpace().getId())
                .devices(devices)
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
                .build();
    }
}
