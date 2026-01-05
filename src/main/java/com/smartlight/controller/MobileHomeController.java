package com.smartlight.controller;

import com.smartlight.dto.MobileHomeDTO;
import com.smartlight.service.MobileHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/homes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MobileHomeController {
    private final MobileHomeService mobileHomeService;

    // Get all homes for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MobileHomeDTO>> getHomesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(mobileHomeService.getHomesByUser(userId));
    }

    // Get home by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getHome(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(mobileHomeService.getHomeById(id));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // Create new home
    @PostMapping
    public ResponseEntity<?> createHome(@RequestBody MobileHomeDTO dto) {
        try {
            MobileHomeDTO created = mobileHomeService.createHome(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Update home
    @PutMapping("/{id}")
    public ResponseEntity<?> updateHome(@PathVariable Long id, @RequestBody MobileHomeDTO dto) {
        try {
            return ResponseEntity.ok(mobileHomeService.updateHome(id, dto));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // Set active home
    @PutMapping("/user/{userId}/active/{homeId}")
    public ResponseEntity<?> setActiveHome(@PathVariable Long userId, @PathVariable Long homeId) {
        try {
            return ResponseEntity.ok(mobileHomeService.setActiveHome(userId, homeId));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Delete home
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHome(@PathVariable Long id) {
        try {
            mobileHomeService.deleteHome(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
