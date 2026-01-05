package com.smartlight.service;

import com.smartlight.dto.MobileDeviceDTO;
import com.smartlight.entity.MobileDevice;
import com.smartlight.entity.User;
import com.smartlight.repository.MobileDeviceRepository;
import com.smartlight.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MobileDeviceService {
    private final MobileDeviceRepository mobileDeviceRepository;
    private final UserRepository userRepository;
    private final MqttService mqttService;

    public List<MobileDeviceDTO> getDevicesByUser(Long userId) {
        return mobileDeviceRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public MobileDeviceDTO getDeviceById(Long id) {
        MobileDevice device = mobileDeviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        return toDTO(device);
    }

    public MobileDeviceDTO getDeviceByCode(String deviceCode) {
        MobileDevice device = mobileDeviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        return toDTO(device);
    }

    @Transactional
    public MobileDeviceDTO createDevice(MobileDeviceDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if device code already exists
        if (dto.getDeviceCode() != null && mobileDeviceRepository.existsByDeviceCode(dto.getDeviceCode())) {
            throw new RuntimeException("Device code already exists");
        }

        // Generate device code if not provided
        String deviceCode = dto.getDeviceCode();
        if (deviceCode == null || deviceCode.isEmpty()) {
            deviceCode = "DEV_" + System.currentTimeMillis();
        }

        // Default topic if not provided
        String topic = dto.getTopic();
        if (topic == null || topic.isEmpty()) {
            topic = "home/" + deviceCode;
        }

        MobileDevice device = MobileDevice.builder()
                .deviceCode(deviceCode)
                .name(dto.getName())
                .type(dto.getType() != null ? dto.getType() : "lamp")
                .topic(topic)
                .status(false)
                .brightness(dto.getBrightness() != null ? dto.getBrightness() : 100)
                .color(dto.getColor())
                .user(user)
                .roomName(dto.getRoomName())
                .build();

        mobileDeviceRepository.save(device);
        return toDTO(device);
    }

    @Transactional
    public MobileDeviceDTO updateDevice(Long id, MobileDeviceDTO dto) {
        MobileDevice device = mobileDeviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        if (dto.getName() != null) {
            device.setName(dto.getName());
        }
        if (dto.getType() != null) {
            device.setType(dto.getType());
        }
        if (dto.getTopic() != null) {
            device.setTopic(dto.getTopic());
        }
        if (dto.getRoomName() != null) {
            device.setRoomName(dto.getRoomName());
        }
        if (dto.getBrightness() != null) {
            device.setBrightness(dto.getBrightness());
        }
        if (dto.getColor() != null) {
            device.setColor(dto.getColor());
        }

        mobileDeviceRepository.save(device);
        return toDTO(device);
    }

    @Transactional
    public MobileDeviceDTO updateDeviceStatus(Long id, Boolean status) {
        MobileDevice device = mobileDeviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        device.setStatus(status);
        mobileDeviceRepository.save(device);

        // Send MQTT command
        String command = status ? "1" : "0";
        String topic = device.getTopic();
        
        // Use /light/control for lamp devices
        if ("lamp".equalsIgnoreCase(device.getType())) {
            topic = "/light/control";
        }
        
        System.out.println("MobileDevice - MQTT publish: topic=" + topic + ", command=" + command);
        mqttService.publish(topic, command);

        return toDTO(device);
    }

    @Transactional
    public MobileDeviceDTO toggleDevice(Long id) {
        MobileDevice device = mobileDeviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        boolean newStatus = !device.getStatus();
        return updateDeviceStatus(id, newStatus);
    }

    @Transactional
    public void deleteDevice(Long id) {
        mobileDeviceRepository.deleteById(id);
    }

    private MobileDeviceDTO toDTO(MobileDevice device) {
        return MobileDeviceDTO.builder()
                .id(device.getId())
                .deviceCode(device.getDeviceCode())
                .name(device.getName())
                .type(device.getType())
                .topic(device.getTopic())
                .status(device.getStatus())
                .brightness(device.getBrightness())
                .color(device.getColor())
                .userId(device.getUser().getId())
                .roomName(device.getRoomName())
                .build();
    }
}
