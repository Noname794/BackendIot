package com.smartlight.controller;

import com.smartlight.dto.*;
import com.smartlight.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SpaceController {
    private final SpaceService spaceService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<SpaceDTO>> getSpaces(@RequestHeader("Authorization") String authHeader) {
        String email = extractEmail(authHeader);
        return ResponseEntity.ok(spaceService.getSpacesByUser(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpaceDTO> getSpace(@PathVariable Long id) {
        return ResponseEntity.ok(spaceService.getSpaceById(id));
    }

    @PostMapping
    public ResponseEntity<SpaceDTO> createSpace(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody SpaceDTO dto) {
        String email = extractEmail(authHeader);
        return ResponseEntity.ok(spaceService.createSpace(email, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpaceDTO> updateSpace(@PathVariable Long id, @RequestBody SpaceDTO dto) {
        return ResponseEntity.ok(spaceService.updateSpace(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id) {
        spaceService.deleteSpace(id);
        return ResponseEntity.ok().build();
    }

    private String extractEmail(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtService.extractEmail(token);
    }
}
