package com.smartlight.service;

import com.smartlight.dto.ScenarioDTO;
import com.smartlight.entity.Scenario;
import com.smartlight.entity.Space;
import com.smartlight.entity.User;
import com.smartlight.repository.ScenarioRepository;
import com.smartlight.repository.SpaceRepository;
import com.smartlight.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScenarioService {
    private final ScenarioRepository scenarioRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;

    public List<ScenarioDTO> getScenariosByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return scenarioRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ScenarioDTO> getAllActiveScenarios() {
        return scenarioRepository.findByActiveTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ScenarioDTO getScenarioById(Long id) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scenario not found"));
        return toDTO(scenario);
    }

    @Transactional
    public ScenarioDTO createScenario(String email, ScenarioDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Scenario scenario = Scenario.builder()
                .name(dto.getName())
                .timeOn(dto.getTimeOn())
                .timeOff(dto.getTimeOff())
                .timeOnPeriod(dto.getTimeOnPeriod())
                .timeOffPeriod(dto.getTimeOffPeriod())
                .scheduleType(dto.getScheduleType())
                .selectedDates(dto.getSelectedDates())
                .selectedMonth(dto.getSelectedMonth())
                .selectedYear(dto.getSelectedYear())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .scheduleEnabled(dto.getScheduleEnabled() != null ? dto.getScheduleEnabled() : true)
                .deviceStatus(dto.getDeviceStatus() != null ? dto.getDeviceStatus() : true)
                .volume(dto.getVolume() != null ? dto.getVolume() : 70)
                .imageUrl(dto.getImageUrl())
                .user(user)
                .build();

        // Set device and room IDs
        if (dto.getDeviceIds() != null) {
            scenario.setDeviceIdList(dto.getDeviceIds());
        }
        if (dto.getRoomIds() != null) {
            scenario.setRoomIdList(dto.getRoomIds());
        }

        if (dto.getSpaceId() != null) {
            Space space = spaceRepository.findById(dto.getSpaceId())
                    .orElseThrow(() -> new RuntimeException("Space not found"));
            scenario.setSpace(space);
        }

        Scenario saved = scenarioRepository.save(scenario);
        log.info("Created scenario: {} with timeOn: {} {}, timeOff: {} {}", 
                saved.getName(), saved.getTimeOn(), saved.getTimeOnPeriod(),
                saved.getTimeOff(), saved.getTimeOffPeriod());
        return toDTO(saved);
    }

    @Transactional
    public ScenarioDTO updateScenario(Long id, ScenarioDTO dto) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scenario not found"));

        if (dto.getName() != null) scenario.setName(dto.getName());
        if (dto.getTimeOn() != null) scenario.setTimeOn(dto.getTimeOn());
        if (dto.getTimeOff() != null) scenario.setTimeOff(dto.getTimeOff());
        if (dto.getTimeOnPeriod() != null) scenario.setTimeOnPeriod(dto.getTimeOnPeriod());
        if (dto.getTimeOffPeriod() != null) scenario.setTimeOffPeriod(dto.getTimeOffPeriod());
        if (dto.getScheduleType() != null) scenario.setScheduleType(dto.getScheduleType());
        if (dto.getSelectedDates() != null) scenario.setSelectedDates(dto.getSelectedDates());
        if (dto.getSelectedMonth() != null) scenario.setSelectedMonth(dto.getSelectedMonth());
        if (dto.getSelectedYear() != null) scenario.setSelectedYear(dto.getSelectedYear());
        if (dto.getActive() != null) scenario.setActive(dto.getActive());
        if (dto.getScheduleEnabled() != null) scenario.setScheduleEnabled(dto.getScheduleEnabled());
        if (dto.getDeviceStatus() != null) scenario.setDeviceStatus(dto.getDeviceStatus());
        if (dto.getVolume() != null) scenario.setVolume(dto.getVolume());
        if (dto.getImageUrl() != null) scenario.setImageUrl(dto.getImageUrl());
        if (dto.getDeviceIds() != null) scenario.setDeviceIdList(dto.getDeviceIds());
        if (dto.getRoomIds() != null) scenario.setRoomIdList(dto.getRoomIds());

        if (dto.getSpaceId() != null) {
            Space space = spaceRepository.findById(dto.getSpaceId())
                    .orElseThrow(() -> new RuntimeException("Space not found"));
            scenario.setSpace(space);
        }

        Scenario saved = scenarioRepository.save(scenario);
        log.info("Updated scenario: {} with timeOn: {} {}, timeOff: {} {}", 
                saved.getName(), saved.getTimeOn(), saved.getTimeOnPeriod(),
                saved.getTimeOff(), saved.getTimeOffPeriod());
        return toDTO(saved);
    }

    @Transactional
    public void updateLastExecutedOn(Long id) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scenario not found"));
        scenario.setLastExecutedOn(java.time.LocalDateTime.now());
        scenarioRepository.save(scenario);
    }

    @Transactional
    public void updateLastExecutedOff(Long id) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scenario not found"));
        scenario.setLastExecutedOff(java.time.LocalDateTime.now());
        scenarioRepository.save(scenario);
    }

    @Transactional
    public void deleteScenario(Long id) {
        if (!scenarioRepository.existsById(id)) {
            throw new RuntimeException("Scenario not found");
        }
        scenarioRepository.deleteById(id);
        log.info("Deleted scenario with id: {}", id);
    }

    @Transactional
    public ScenarioDTO toggleScenario(Long id) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scenario not found"));
        scenario.setActive(!scenario.getActive());
        Scenario saved = scenarioRepository.save(scenario);
        log.info("Toggled scenario {} to active: {}", saved.getName(), saved.getActive());
        return toDTO(saved);
    }

    private ScenarioDTO toDTO(Scenario scenario) {
        return ScenarioDTO.builder()
                .id(scenario.getId())
                .name(scenario.getName())
                .timeOn(scenario.getTimeOn())
                .timeOff(scenario.getTimeOff())
                .timeOnPeriod(scenario.getTimeOnPeriod())
                .timeOffPeriod(scenario.getTimeOffPeriod())
                .scheduleType(scenario.getScheduleType())
                .selectedDates(scenario.getSelectedDates())
                .selectedMonth(scenario.getSelectedMonth())
                .selectedYear(scenario.getSelectedYear())
                .active(scenario.getActive())
                .scheduleEnabled(scenario.getScheduleEnabled())
                .deviceStatus(scenario.getDeviceStatus())
                .volume(scenario.getVolume())
                .deviceIds(scenario.getDeviceIdList())
                .roomIds(scenario.getRoomIdList())
                .spaceId(scenario.getSpace() != null ? scenario.getSpace().getId() : null)
                .imageUrl(scenario.getImageUrl())
                .createdAt(scenario.getCreatedAt() != null ? scenario.getCreatedAt().toString() : null)
                .updatedAt(scenario.getUpdatedAt() != null ? scenario.getUpdatedAt().toString() : null)
                .lastExecutedOn(scenario.getLastExecutedOn() != null ? scenario.getLastExecutedOn().toString() : null)
                .lastExecutedOff(scenario.getLastExecutedOff() != null ? scenario.getLastExecutedOff().toString() : null)
                .build();
    }
}
