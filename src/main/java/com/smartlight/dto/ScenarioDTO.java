package com.smartlight.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioDTO {
    private Long id;
    private String name;
    private String timeOn;
    private String timeOff;
    private String timeOnPeriod;
    private String timeOffPeriod;
    private String scheduleType;
    private List<Integer> selectedDates;
    private Integer selectedMonth;
    private Integer selectedYear;
    private Boolean active;
    private Boolean scheduleEnabled;
    private Boolean deviceStatus;
    private Integer volume;
    private List<Long> deviceIds;
    private List<Long> roomIds;
    private Long spaceId;
    private String imageUrl;
    private String createdAt;
    private String updatedAt;
    private String lastExecutedOn;
    private String lastExecutedOff;
}
