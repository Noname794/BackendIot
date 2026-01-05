package com.smartlight.repository;

import com.smartlight.entity.Scenario;
import com.smartlight.entity.User;
import com.smartlight.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    List<Scenario> findByUser(User user);
    List<Scenario> findByUserOrderByCreatedAtDesc(User user);
    List<Scenario> findBySpace(Space space);
    List<Scenario> findByUserAndActive(User user, Boolean active);
    List<Scenario> findByActiveTrue();
    List<Scenario> findByActiveTrueAndScheduleEnabledTrue();
}
