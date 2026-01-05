package com.smartlight.controller;

import com.smartlight.dto.*;
import com.smartlight.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomController {
    private final RoomService roomService;

    @GetMapping("/spaces/{spaceId}/rooms")
    public ResponseEntity<List<RoomDTO>> getRoomsBySpace(@PathVariable Long spaceId) {
        return ResponseEntity.ok(roomService.getRoomsBySpace(spaceId));
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<RoomDTO> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PostMapping("/spaces/{spaceId}/rooms")
    public ResponseEntity<RoomDTO> createRoom(@PathVariable Long spaceId, @RequestBody RoomDTO dto) {
        return ResponseEntity.ok(roomService.createRoom(spaceId, dto));
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable Long id, @RequestBody RoomDTO dto) {
        return ResponseEntity.ok(roomService.updateRoom(id, dto));
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok().build();
    }
}
