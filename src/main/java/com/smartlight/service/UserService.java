package com.smartlight.service;

import com.smartlight.dto.*;
import com.smartlight.entity.User;
import com.smartlight.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    
    // Store reset codes temporarily (in production, use Redis or database)
    private final Map<String, ResetCodeInfo> resetCodes = new ConcurrentHashMap<>();

    private static class ResetCodeInfo {
        String code;
        LocalDateTime expiresAt;
        
        ResetCodeInfo(String code) {
            this.code = code;
            this.expiresAt = LocalDateTime.now().plusMinutes(10); // Code expires in 10 minutes
        }
        
        boolean isValid(String inputCode) {
            return code.equals(inputCode) && LocalDateTime.now().isBefore(expiresAt);
        }
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(toDTO(user))
                .message("Registration successful")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(toDTO(user))
                .message("Login successful")
                .build();
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toDTO(user);
    }

    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDTO updateUser(String email, UserDTO updateRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update fields
        if (updateRequest.getUsername() != null && !updateRequest.getUsername().isEmpty()) {
            user.setUsername(updateRequest.getUsername());
        }
        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }
        // Note: Email update requires additional validation (check if new email exists)
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().isEmpty() 
            && !updateRequest.getEmail().equals(email)) {
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(updateRequest.getEmail());
        }

        userRepository.save(user);
        return toDTO(user);
    }

    public String generateResetCode(String email) {
        // Check if email exists
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email not found");
        }
        
        // Generate 4-digit code
        String code = String.format("%04d", new Random().nextInt(10000));
        
        // Store the code
        resetCodes.put(email, new ResetCodeInfo(code));
        
        // Send email with the code
        try {
            emailService.sendResetCode(email, code);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
        
        // Don't return the code in production (return null or empty)
        return null;
    }

    public boolean verifyResetCode(String email, String code) {
        ResetCodeInfo info = resetCodes.get(email);
        if (info == null) {
            return false;
        }
        return info.isValid(code);
    }

    public void resetPassword(String email, String code, String newPassword) {
        // Verify code first
        if (!verifyResetCode(email, code)) {
            throw new RuntimeException("Invalid or expired code");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Remove used code
        resetCodes.remove(email);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }
}
