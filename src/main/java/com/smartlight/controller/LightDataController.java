package com.smartlight.controller;

import com.smartlight.entity.LightData;
import com.smartlight.repository.LightDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LightDataController {

    private final LightDataRepository lightDataRepository;

    @GetMapping("/power")
    public ResponseEntity<Map<String, Object>> getPowerStatistics(
            @RequestParam(defaultValue = "week") String period) {
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        
        switch (period.toLowerCase()) {
            case "today":
                startDate = now.toLocalDate().atStartOfDay();
                break;
            case "week":
                startDate = now.minusDays(7);
                break;
            case "month":
                startDate = now.minusMonths(1);
                break;
            case "quarter":
                startDate = now.minusMonths(3);
                break;
            case "year":
                startDate = now.minusYears(1);
                break;
            default:
                startDate = now.minusDays(7);
        }
        
        List<LightData> data = lightDataRepository.findByTimestampBetween(startDate, now);
        
        // Calculate total power consumption
        double totalPower = data.stream()
                .mapToDouble(LightData::getPower)
                .sum();
        
        // Calculate average power
        double avgPower = data.isEmpty() ? 0 : totalPower / data.size();
        
        // Get latest reading
        LightData latest = data.isEmpty() ? null : data.get(data.size() - 1);
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalPower", Math.round(totalPower * 100.0) / 100.0);
        response.put("averagePower", Math.round(avgPower * 100.0) / 100.0);
        response.put("dataPoints", data.size());
        response.put("period", period);
        response.put("latestStatus", latest != null ? latest.getStatus() : "unknown");
        response.put("latestCurrent", latest != null ? latest.getCurrent() : 0);
        response.put("latestPower", latest != null ? latest.getPower() : 0);
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<LightData> getLatestData() {
        List<LightData> data = lightDataRepository.findLatest();
        if (data.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(data.get(0));
    }

    @GetMapping("/history")
    public ResponseEntity<List<LightData>> getHistory(
            @RequestParam(defaultValue = "100") int limit) {
        List<LightData> data = lightDataRepository.findLatest();
        if (data.size() > limit) {
            data = data.subList(0, limit);
        }
        return ResponseEntity.ok(data);
    }
}
