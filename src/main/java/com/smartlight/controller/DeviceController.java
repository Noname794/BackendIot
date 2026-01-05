package com.smartlight.controller;

import com.smartlight.dto.*;
import com.smartlight.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/web")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeviceController {
    private final DeviceService deviceService;
    private final MqttService mqttService;

    @GetMapping("/spaces/{spaceId}/devices")
    public ResponseEntity<List<DeviceDTO>> getDevicesBySpace(@PathVariable Long spaceId) {
        return ResponseEntity.ok(deviceService.getDevicesBySpace(spaceId));
    }

    @GetMapping("/rooms/{roomId}/devices")
    public ResponseEntity<List<DeviceDTO>> getDevicesByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(deviceService.getDevicesByRoom(roomId));
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<DeviceDTO> getDevice(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @PostMapping("/spaces/{spaceId}/devices")
    public ResponseEntity<DeviceDTO> createDevice(@PathVariable Long spaceId, @RequestBody DeviceDTO dto) {
        return ResponseEntity.ok(deviceService.createDevice(spaceId, dto));
    }

    @PutMapping("/devices/{id}")
    public ResponseEntity<DeviceDTO> updateDevice(@PathVariable Long id, @RequestBody DeviceDTO dto) {
        return ResponseEntity.ok(deviceService.updateDevice(id, dto));
    }

    @PostMapping("/devices/{id}/toggle")
    public ResponseEntity<DeviceDTO> toggleDevice(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.toggleDevice(id));
    }

    @PostMapping("/devices/{id}/state")
    public ResponseEntity<DeviceDTO> setDeviceState(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean state = body.getOrDefault("state", false);
        return ResponseEntity.ok(deviceService.setDeviceState(id, state));
    }

    @DeleteMapping("/devices/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/mqtt/status")
    public ResponseEntity<Map<String, Object>> getMqttStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("connected", mqttService.isConnected());
        status.put("lastLightStatus", mqttService.getLastStatus());
        status.put("lastCurrent", mqttService.getLastCurrent());
        status.put("lastPower", mqttService.getLastPower());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/mqtt/control")
    public ResponseEntity<Map<String, String>> sendMqttCommand(@RequestBody Map<String, String> body) {
        String command = body.getOrDefault("command", "0");
        String topic = body.getOrDefault("topic", "/light/control");
        mqttService.publish(topic, command);
        Map<String, String> response = new HashMap<>();
        response.put("status", "sent");
        response.put("topic", topic);
        response.put("command", command);
        return ResponseEntity.ok(response);
    }
}
