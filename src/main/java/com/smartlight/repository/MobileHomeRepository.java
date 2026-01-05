package com.smartlight.repository;

import com.smartlight.entity.MobileHome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MobileHomeRepository extends JpaRepository<MobileHome, Long> {
    List<MobileHome> findByUserId(Long userId);
    Optional<MobileHome> findByUserIdAndIsActiveTrue(Long userId);
    
    @Modifying
    @Query("UPDATE MobileHome h SET h.isActive = false WHERE h.user.id = :userId")
    void deactivateAllByUserId(Long userId);
}
