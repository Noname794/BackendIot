package com.smartlight.controller;

import com.smartlight.dto.ScenarioDTO;
import com.smartlight.service.JwtService;
import com.smartlight.service.ScenarioService;
import com.smartlight.service.ScenarioSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scenarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScenarioController {
    private final ScenarioService scenarioService;
    private final ScenarioSchedulerService scenarioSchedulerService;
    private final JwtService jwtService;

    private String extractEmail(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtService.extractEmail(token);
    }

    @GetMapping
    public ResponseEntity<?> getAllScenarios(@RequestHeader("Authorization") String authHeader) {
        try {
            String email = extractEmail(authHeader);
            List<ScenarioDTO> scenarios = scenarioService.getScenariosByUser(email);
            return ResponseEntity.ok(scenarios);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getScenarioById(@PathVariable Long id) {
        try {
            ScenarioDTO scenario = scenarioService.getScenarioById(id);
            return ResponseEntity.ok(scenario);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createScenario(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ScenarioDTO dto) {
        try {
            String email = extractEmail(authHeader);
            ScenarioDTO created = scenarioService.createScenario(email, dto);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateScenario(
            @PathVariable Long id,
            @RequestBody ScenarioDTO dto) {
        try {
            ScenarioDTO updated = scenarioService.updateScenario(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteScenario(@PathVariable Long id) {
        try {
            scenarioService.deleteScenario(id);
            return ResponseEntity.ok(Map.of("message", "Scenario deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<?> toggleScenario(@PathVariable Long id) {
        try {
            ScenarioDTO toggled = scenarioService.toggleScenario(id);
            return ResponseEntity.ok(toggled);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/trigger")
    public ResponseEntity<?> triggerScenario(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        try {
            boolean turnOn = body.getOrDefault("turnOn", true);
            scenarioSchedulerService.triggerScenario(id, turnOn);
            return ResponseEntity.ok(Map.of(
                    "message", "Scenario triggered successfully",
                    "action", turnOn ? "ON" : "OFF"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
