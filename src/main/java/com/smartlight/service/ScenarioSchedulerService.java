package com.smartlight.service;

import com.smartlight.entity.Device;
import com.smartlight.entity.Scenario;
import com.smartlight.repository.DeviceRepository;
import com.smartlight.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScenarioSchedulerService {
    
    private final ScenarioRepository scenarioRepository;
    private final DeviceRepository deviceRepository;
    private final MqttService mqttService;
    private final ScenarioService scenarioService;

    // Run every minute to check scenarios
    @Scheduled(cron = "0 * * * * *")
    @Transactional(readOnly = true)
    public void checkAndExecuteScenarios() {
        log.info("Checking scenarios at {}", LocalDateTime.now());
        
        List<Scenario> activeScenarios = scenarioRepository.findByActiveTrueAndScheduleEnabledTrue();
        log.info("Found {} active scenarios", activeScenarios.size());
        
        for (Scenario scenario : activeScenarios) {
            try {
                checkScenario(scenario);
            } catch (Exception e) {
                log.error("Error checking scenario {}: {}", scenario.getName(), e.getMessage());
            }
        }
    }

    private void checkScenario(Scenario scenario) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        
        log.info("Checking scenario: {} - scheduleType: {}, timeOn: {} {}, timeOff: {} {}", 
                scenario.getName(), scenario.getScheduleType(),
                scenario.getTimeOn(), scenario.getTimeOnPeriod(),
                scenario.getTimeOff(), scenario.getTimeOffPeriod());
        
        // Check if today matches the schedule
        if (!shouldRunToday(scenario, today)) {
            log.info("Scenario {} should not run today", scenario.getName());
            return;
        }

        // Parse time on
        LocalTime timeOn = parseTime(scenario.getTimeOn(), scenario.getTimeOnPeriod());
        LocalTime timeOff = parseTime(scenario.getTimeOff(), scenario.getTimeOffPeriod());

        log.info("Parsed times - timeOn: {}, timeOff: {}, currentTime: {}", timeOn, timeOff, currentTime);

        if (timeOn == null || timeOff == null) {
            log.warn("Invalid time configuration for scenario: {}", scenario.getName());
            return;
        }

        // Check if it's time to turn ON
        if (isTimeToExecute(currentTime, timeOn, scenario.getLastExecutedOn(), today)) {
            log.info(">>> Executing ON for scenario: {} at {}", scenario.getName(), currentTime);
            executeScenarioAction(scenario, true);
            scenarioService.updateLastExecutedOn(scenario.getId());
        }

        // Check if it's time to turn OFF
        if (isTimeToExecute(currentTime, timeOff, scenario.getLastExecutedOff(), today)) {
            log.info(">>> Executing OFF for scenario: {} at {}", scenario.getName(), currentTime);
            executeScenarioAction(scenario, false);
            scenarioService.updateLastExecutedOff(scenario.getId());
        }
    }

    private boolean shouldRunToday(Scenario scenario, LocalDate today) {
        String scheduleType = scenario.getScheduleType();
        
        log.debug("Checking shouldRunToday - scheduleType: {}, today: {}", scheduleType, today);
        
        if (scheduleType == null || scheduleType.equals("everyday")) {
            log.debug("Schedule type is everyday - returning true");
            return true;
        }
        
        if (scheduleType.equals("weekdays")) {
            DayOfWeek dayOfWeek = today.getDayOfWeek();
            boolean isWeekday = dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
            log.debug("Schedule type is weekdays - dayOfWeek: {}, isWeekday: {}", dayOfWeek, isWeekday);
            return isWeekday;
        }
        
        if (scheduleType.equals("custom")) {
            // Check if today's date is in selected dates
            List<Integer> selectedDates = scenario.getSelectedDates();
            Integer selectedMonth = scenario.getSelectedMonth();
            Integer selectedYear = scenario.getSelectedYear();
            
            log.debug("Custom schedule - selectedDates: {}, selectedMonth: {}, selectedYear: {}", 
                    selectedDates, selectedMonth, selectedYear);
            
            if (selectedDates == null || selectedDates.isEmpty()) {
                log.debug("No selected dates - returning false");
                return false;
            }
            
            // Check month and year if specified
            if (selectedMonth != null && selectedMonth != today.getMonthValue()) {
                log.debug("Month mismatch - selected: {}, today: {}", selectedMonth, today.getMonthValue());
                return false;
            }
            if (selectedYear != null && selectedYear != today.getYear()) {
                log.debug("Year mismatch - selected: {}, today: {}", selectedYear, today.getYear());
                return false;
            }
            
            boolean containsToday = selectedDates.contains(today.getDayOfMonth());
            log.debug("Contains today ({}): {}", today.getDayOfMonth(), containsToday);
            return containsToday;
        }
        
        return false;
    }

    private LocalTime parseTime(String time, String period) {
        if (time == null || time.isEmpty()) {
            return null;
        }
        
        try {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            
            log.debug("Parsing time: {} {} -> hour={}, minute={}", time, period, hour, minute);
            
            // Convert 12-hour to 24-hour format
            if (period != null) {
                if (period.equalsIgnoreCase("PM") && hour != 12) {
                    hour += 12;
                } else if (period.equalsIgnoreCase("AM") && hour == 12) {
                    hour = 0;
                }
            }
            
            // Handle case where hour might already be in 24-hour format
            if (hour > 23) hour = hour % 24;
            
            LocalTime result = LocalTime.of(hour, minute);
            log.debug("Parsed result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error parsing time: {} {} - {}", time, period, e.getMessage());
            return null;
        }
    }

    private boolean isTimeToExecute(LocalTime currentTime, LocalTime targetTime, 
                                     LocalDateTime lastExecuted, LocalDate today) {
        // Check if current time matches target time (within 1 minute window)
        int currentMinutes = currentTime.getHour() * 60 + currentTime.getMinute();
        int targetMinutes = targetTime.getHour() * 60 + targetTime.getMinute();
        
        if (currentMinutes != targetMinutes) {
            return false;
        }
        
        // Check if already executed today
        if (lastExecuted != null && lastExecuted.toLocalDate().equals(today)) {
            return false;
        }
        
        return true;
    }

    private void executeScenarioAction(Scenario scenario, boolean turnOn) {
        String command = turnOn ? "1" : "0";
        
        // Always send to the main light control topic that ESP32 listens to
        String defaultTopic = "/light/control";
        
        List<Long> deviceIds = scenario.getDeviceIdList();
        
        if (deviceIds == null || deviceIds.isEmpty()) {
            // If no specific devices, send to default topic
            mqttService.publish(defaultTopic, command);
            log.info("Scenario {} - Sent {} to {}", scenario.getName(), command, defaultTopic);
            return;
        }
        
        // For now, send to the main control topic regardless of device
        // ESP32 listens on /light/control
        mqttService.publish(defaultTopic, command);
        log.info("Scenario {} - Sent {} to {}", scenario.getName(), command, defaultTopic);
        
        // Update device states in database
        for (Long deviceId : deviceIds) {
            try {
                Device device = deviceRepository.findById(deviceId).orElse(null);
                if (device != null) {
                    device.setIsOn(turnOn);
                    deviceRepository.save(device);
                    log.info("Scenario {} - Updated device {} state to {}", 
                            scenario.getName(), device.getName(), turnOn ? "ON" : "OFF");
                }
            } catch (Exception e) {
                log.error("Error updating device {}: {}", deviceId, e.getMessage());
            }
        }
    }

    // Manual trigger for testing
    @Transactional
    public void triggerScenario(Long scenarioId, boolean turnOn) {
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new RuntimeException("Scenario not found"));
        
        log.info("Manually triggering scenario: {} - {}", scenario.getName(), turnOn ? "ON" : "OFF");
        executeScenarioAction(scenario, turnOn);
        
        if (turnOn) {
            scenarioService.updateLastExecutedOn(scenarioId);
        } else {
            scenarioService.updateLastExecutedOff(scenarioId);
        }
    }
}
