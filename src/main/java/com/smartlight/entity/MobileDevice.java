package com.smartlight.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MobileDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_code", unique = true)
    private String deviceCode;

    @Column(nullable = false)
    private String name;

    @Column(name = "type")
    private String type; // lamp, cctv, speaker, ac

    @Column(name = "topic")
    private String topic; // MQTT topic

    @Column(name = "status")
    private Boolean status = false;

    @Column(name = "brightness")
    private Integer brightness = 100;

    @Column(name = "color")
    private String color; // hex color for RGB

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "room_name")
    private String roomName;

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
}
