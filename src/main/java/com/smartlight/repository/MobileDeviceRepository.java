package com.smartlight.repository;

import com.smartlight.entity.MobileDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MobileDeviceRepository extends JpaRepository<MobileDevice, Long> {
    List<MobileDevice> findByUserId(Long userId);
    Optional<MobileDevice> findByDeviceCode(String deviceCode);
    boolean existsByDeviceCode(String deviceCode);
    List<MobileDevice> findByUserIdAndType(Long userId, String type);
    List<MobileDevice> findByUserIdAndRoomName(Long userId, String roomName);
}
