package com.smartlight.controller;

import com.smartlight.dto.MobileDeviceDTO;
import com.smartlight.service.MobileDeviceService;
import com.smartlight.service.MqttService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MobileDeviceController {
    private final MobileDeviceService mobileDeviceService;
    private final MqttService mqttService;

    // Get all devices for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MobileDeviceDTO>> getDevicesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(mobileDeviceService.getDevicesByUser(userId));
    }

    // Get device by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getDevice(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(mobileDeviceService.getDeviceById(id));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // Create new device
    @PostMapping
    public ResponseEntity<?> createDevice(@RequestBody MobileDeviceDTO dto) {
        try {
            MobileDeviceDTO created = mobileDeviceService.createDevice(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Update device
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDevice(@PathVariable Long id, @RequestBody MobileDeviceDTO dto) {
        try {
            return ResponseEntity.ok(mobileDeviceService.updateDevice(id, dto));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // Update device status (on/off)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateDeviceStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        try {
            Boolean status = body.get("status");
            if (status == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Status is required");
                return ResponseEntity.badRequest().body(error);
            }
            return ResponseEntity.ok(mobileDeviceService.updateDeviceStatus(id, status));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // Toggle device
    @PostMapping("/{id}/toggle")
    public ResponseEntity<?> toggleDevice(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(mobileDeviceService.toggleDevice(id));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // Delete device
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        mobileDeviceService.deleteDevice(id);
        return ResponseEntity.ok().build();
    }

    // Send MQTT command directly (for advanced control)
    @PostMapping("/mqtt/send")
    public ResponseEntity<Map<String, String>> sendMqttCommand(@RequestBody Map<String, String> body) {
        String topic = body.getOrDefault("topic", "/light/control");
        String command = body.getOrDefault("command", "0");
        
        mqttService.publish(topic, command);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "sent");
        response.put("topic", topic);
        response.put("command", command);
        return ResponseEntity.ok(response);
    }
}
