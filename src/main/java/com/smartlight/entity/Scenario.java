package com.smartlight.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "scenarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "time_on")
    private String timeOn; // Format: HH:mm

    @Column(name = "time_off")
    private String timeOff; // Format: HH:mm

    @Column(name = "time_on_period")
    private String timeOnPeriod; // AM or PM

    @Column(name = "time_off_period")
    private String timeOffPeriod; // AM or PM

    @Column(name = "schedule_type")
    private String scheduleType; // everyday, weekdays, custom

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "scenario_selected_dates", joinColumns = @JoinColumn(name = "scenario_id"))
    @Column(name = "selected_date")
    private List<Integer> selectedDates;

    @Column(name = "selected_month")
    private Integer selectedMonth;

    @Column(name = "selected_year")
    private Integer selectedYear;

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "schedule_enabled")
    private Boolean scheduleEnabled = true;

    // Device settings
    @Column(name = "device_status")
    private Boolean deviceStatus = true; // ON/OFF status for devices

    @Column(name = "volume")
    private Integer volume = 70;

    // Store device IDs as comma-separated string
    @Column(name = "device_ids", length = 1000)
    private String deviceIds;

    // Store room IDs as comma-separated string
    @Column(name = "room_ids", length = 1000)
    private String roomIds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private Space space;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "last_executed_on")
    private LocalDateTime lastExecutedOn;

    @Column(name = "last_executed_off")
    private LocalDateTime lastExecutedOff;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public List<Long> getDeviceIdList() {
        if (deviceIds == null || deviceIds.isEmpty()) return List.of();
        return java.util.Arrays.stream(deviceIds.split(","))
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
    }

    public void setDeviceIdList(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            this.deviceIds = "";
        } else {
            this.deviceIds = ids.stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
        }
    }

    public List<Long> getRoomIdList() {
        if (roomIds == null || roomIds.isEmpty()) return List.of();
        return java.util.Arrays.stream(roomIds.split(","))
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
    }

    public void setRoomIdList(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            this.roomIds = "";
        } else {
            this.roomIds = ids.stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
        }
    }
}
