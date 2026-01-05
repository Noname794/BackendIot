package com.smartlight.controller;

import com.smartlight.entity.LightData;
import com.smartlight.repository.LightDataRepository;
import com.smartlight.service.MqttService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/light")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LightController {
    private final LightDataRepository repository;
    private final MqttService mqttService;

    @PostMapping("/control")
    public ResponseEntity<String> control(@RequestBody Map<String, String> request) {
        String command = request.get("command"); // "1" or "0"
        mqttService.publishControl(command);
        return ResponseEntity.ok("Command sent: " + command);
    }

    @PostMapping("/reset-wifi")
    public ResponseEntity<Map<String, Object>> resetWifi() {
        try {
            mqttService.publishControl("reset_wifi");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reset WiFi command sent. ESP32 will restart and enter provisioning mode."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to send reset command: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<LightData>> getHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        if (start != null && end != null) {
            return ResponseEntity.ok(repository.findByTimestampBetween(start, end));
        }
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/latest")
    public ResponseEntity<LightData> getLatest() {
        List<LightData> data = repository.findLatest();
        if (data.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(data.get(0));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<LightData> allData = repository.findAll();
        
        double avgCurrent = allData.stream()
                .mapToDouble(LightData::getCurrent)
                .average()
                .orElse(0.0);
        
        double avgPower = allData.stream()
                .mapToDouble(LightData::getPower)
                .average()
                .orElse(0.0);
        
        long onCount = allData.stream()
                .filter(d -> "on".equals(d.getStatus()))
                .count();
        
        return ResponseEntity.ok(Map.of(
            "totalRecords", allData.size(),
            "avgCurrent", avgCurrent,
            "avgPower", avgPower,
            "onCount", onCount,
            "offCount", allData.size() - onCount
        ));
    }
}
