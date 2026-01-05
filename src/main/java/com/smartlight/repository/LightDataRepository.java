package com.smartlight.repository;

import com.smartlight.entity.LightData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LightDataRepository extends JpaRepository<LightData, Long> {
    List<LightData> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT l FROM LightData l ORDER BY l.timestamp DESC")
    List<LightData> findLatest();
}
