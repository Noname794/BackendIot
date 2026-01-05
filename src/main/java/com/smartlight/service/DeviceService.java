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
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final SpaceRepository spaceRepository;
    private final RoomRepository roomRepository;
    private final MqttService mqttService;

    public List<DeviceDTO> getDevicesBySpace(Long spaceId) {
        return deviceRepository.findBySpaceId(spaceId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<DeviceDTO> getDevicesByRoom(Long roomId) {
        return deviceRepository.findByRoomId(roomId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DeviceDTO getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        return toDTO(device);
    }

    private static final int MAX_DEVICES_PER_ROOM = 6;

    @Transactional
    public DeviceDTO createDevice(Long spaceId, DeviceDTO dto) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new RuntimeException("Space not found"));

        Room room = null;
        if (dto.getRoomId() != null) {
            room = roomRepository.findById(dto.getRoomId())
                    .orElse(null);
            
            // Check if room already has maximum devices
            if (room != null) {
                long deviceCount = deviceRepository.countByRoomId(room.getId());
                if (deviceCount >= MAX_DEVICES_PER_ROOM) {
                    throw new RuntimeException("Room already has maximum " + MAX_DEVICES_PER_ROOM + " devices");
                }
            }
        }

        // Determine MQTT topic - use /light/control for light devices by default
        String mqttTopic = dto.getMqttTopic();
        if (mqttTopic == null || mqttTopic.isEmpty()) {
            String deviceType = dto.getDeviceType() != null ? dto.getDeviceType().toLowerCase() : "light";
            if ("light".equals(deviceType)) {
                mqttTopic = "/light/control";
            } else {
                mqttTopic = "/device/" + System.currentTimeMillis();
            }
        }

        Device device = Device.builder()
                .name(dto.getName())
                .deviceType(dto.getDeviceType())
                .image(dto.getImage())
                .isOn(false)
                .mqttTopic(mqttTopic)
                .space(space)
                .room(room)
                .build();

        deviceRepository.save(device);
        return toDTO(device);
    }

    @Transactional
    public DeviceDTO updateDevice(Long id, DeviceDTO dto) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        device.setName(dto.getName());
        device.setDeviceType(dto.getDeviceType());
        if (dto.getImage() != null) {
            device.setImage(dto.getImage());
        }
        if (dto.getMqttTopic() != null) {
            device.setMqttTopic(dto.getMqttTopic());
        }
        if (dto.getRoomId() != null) {
            Room room = roomRepository.findById(dto.getRoomId()).orElse(null);
            device.setRoom(room);
        }

        deviceRepository.save(device);
        return toDTO(device);
    }

    @Transactional
    public DeviceDTO toggleDevice(Long id) {
        System.out.println("=== TOGGLE DEVICE CALLED === ID: " + id);
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        System.out.println("Device found: " + device.getName());
        System.out.println("Device type: " + device.getDeviceType());
        System.out.println("Device mqttTopic from DB: " + device.getMqttTopic());

        boolean newState = !device.getIsOn();
        System.out.println("Old state: " + device.getIsOn() + " -> New state: " + newState);
        device.setIsOn(newState);
        deviceRepository.save(device);

        // Send MQTT command to control the physical device
        String command = newState ? "1" : "0";
        
        // ALWAYS use /light/control for light devices (ignore database value)
        String topic = "/light/control";
        String deviceType = device.getDeviceType();
        if (deviceType != null && !"light".equalsIgnoreCase(deviceType)) {
            // Only use custom topic for non-light devices
            topic = device.getMqttTopic() != null ? device.getMqttTopic() : "/device/" + id;
        }
        
        System.out.println("=== PUBLISHING MQTT === Topic: " + topic + ", Command: " + command);
        mqttService.publish(topic, command);
        System.out.println("=== MQTT PUBLISH CALLED ===");

        return toDTO(device);
    }

    @Transactional
    public DeviceDTO setDeviceState(Long id, boolean state) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        device.setIsOn(state);
        deviceRepository.save(device);

        // Send MQTT command to control the physical device
        String command = state ? "1" : "0";
        
        // ALWAYS use /light/control for light devices
        String topic = "/light/control";
        String deviceType = device.getDeviceType();
        if (deviceType != null && !"light".equalsIgnoreCase(deviceType)) {
            topic = device.getMqttTopic() != null ? device.getMqttTopic() : "/device/" + id;
        }
        
        System.out.println("Setting device state - MQTT command: " + command + " to topic: " + topic);
        mqttService.publish(topic, command);

        return toDTO(device);
    }

    @Transactional
    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }

    private DeviceDTO toDTO(Device device) {
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
