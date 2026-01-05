package com.smartlight.repository;

import com.smartlight.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findBySpaceId(Long spaceId);
    List<Device> findByRoomId(Long roomId);
    long countByRoomId(Long roomId);
}
