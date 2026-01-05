package com.smartlight.service;

import com.smartlight.dto.MobileHomeDTO;
import com.smartlight.entity.MobileHome;
import com.smartlight.entity.User;
import com.smartlight.repository.MobileHomeRepository;
import com.smartlight.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MobileHomeService {
    private final MobileHomeRepository mobileHomeRepository;
    private final UserRepository userRepository;

    public List<MobileHomeDTO> getHomesByUser(Long userId) {
        return mobileHomeRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public MobileHomeDTO getHomeById(Long id) {
        MobileHome home = mobileHomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Home not found"));
        return toDTO(home);
    }

    @Transactional
    public MobileHomeDTO createHome(MobileHomeDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if this is the first home for user
        List<MobileHome> existingHomes = mobileHomeRepository.findByUserId(dto.getUserId());
        boolean isFirstHome = existingHomes.isEmpty();

        MobileHome home = MobileHome.builder()
                .name(dto.getName())
                .country(dto.getCountry())
                .address(dto.getAddress())
                .rooms(dto.getRooms() != null ? dto.getRooms() : new ArrayList<>())
                .isActive(isFirstHome) // First home is active by default
                .user(user)
                .build();

        mobileHomeRepository.save(home);
        return toDTO(home);
    }

    @Transactional
    public MobileHomeDTO updateHome(Long id, MobileHomeDTO dto) {
        MobileHome home = mobileHomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Home not found"));

        if (dto.getName() != null) {
            home.setName(dto.getName());
        }
        if (dto.getCountry() != null) {
            home.setCountry(dto.getCountry());
        }
        if (dto.getAddress() != null) {
            home.setAddress(dto.getAddress());
        }
        if (dto.getRooms() != null) {
            home.setRooms(dto.getRooms());
        }

        mobileHomeRepository.save(home);
        return toDTO(home);
    }

    @Transactional
    public MobileHomeDTO setActiveHome(Long userId, Long homeId) {
        // Deactivate all homes for user
        mobileHomeRepository.deactivateAllByUserId(userId);

        // Activate the selected home
        MobileHome home = mobileHomeRepository.findById(homeId)
                .orElseThrow(() -> new RuntimeException("Home not found"));
        
        if (!home.getUser().getId().equals(userId)) {
            throw new RuntimeException("Home does not belong to user");
        }

        home.setIsActive(true);
        mobileHomeRepository.save(home);
        return toDTO(home);
    }

    @Transactional
    public void deleteHome(Long id) {
        MobileHome home = mobileHomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Home not found"));
        
        Long userId = home.getUser().getId();
        boolean wasActive = home.getIsActive();
        
        mobileHomeRepository.deleteById(id);

        // If deleted home was active, activate another home if exists
        if (wasActive) {
            List<MobileHome> remainingHomes = mobileHomeRepository.findByUserId(userId);
            if (!remainingHomes.isEmpty()) {
                MobileHome newActive = remainingHomes.get(0);
                newActive.setIsActive(true);
                mobileHomeRepository.save(newActive);
            }
        }
    }

    private MobileHomeDTO toDTO(MobileHome home) {
        return MobileHomeDTO.builder()
                .id(home.getId())
                .name(home.getName())
                .country(home.getCountry())
                .address(home.getAddress())
                .rooms(home.getRooms())
                .isActive(home.getIsActive())
                .userId(home.getUser().getId())
                .build();
    }
}
