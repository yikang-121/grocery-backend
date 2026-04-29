package com.grocery.grocerybackend.controller;

import com.grocery.grocerybackend.dto.ChangePasswordRequest;
import com.grocery.grocerybackend.entity.User;
import com.grocery.grocerybackend.service.AuthService;
import com.grocery.grocerybackend.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        boolean success = authService.register(user);
        if (success) {
            response.put("success", true);
            response.put("message", "Registration successful");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "User with this email already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User loginData) {
        Map<String, Object> response = new HashMap<>();
        User user = authService.login(loginData.getEmail(), loginData.getPassword());
        if (user != null) {
            String token = jwtUtil.generateToken(user.getEmail());
            user.setPassword(null);
            response.put("success", true);
            response.put("token", token);
            response.put("user", user);
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        }
        response.put("success", false);
        response.put("message", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @GetMapping("/check-role")
    public ResponseEntity<Map<String, Object>> checkUserRole(@RequestHeader("Authorization") String tokenHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            String jwt = tokenHeader.startsWith("Bearer ") ? tokenHeader.substring(7) : tokenHeader;
            if (!jwtUtil.validateToken(jwt)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String email = jwtUtil.extractEmail(jwt);
            if (!jwtUtil.validateToken(jwt, email)) {
                response.put("success", false);
                response.put("message", "Token validation failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            User user = authService.getUserByEmail(email);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("success", true);
            response.put("role", user.getRole());
            response.put("isAdmin", "admin".equals(user.getRole()));
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Invalid token format or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader(value = "Authorization", required = false) String tokenHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (tokenHeader != null && !tokenHeader.isBlank()) {
                String jwt = tokenHeader.startsWith("Bearer ") ? tokenHeader.substring(7) : tokenHeader;
                jwtUtil.invalidateToken(jwt);
            }
            response.put("success", true);
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error during logout");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // -------- CHANGE PASSWORD --------
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestHeader("Authorization") String tokenHeader,
            @RequestBody ChangePasswordRequest request) {

        Map<String, Object> response = new HashMap<>();

        if (request == null || request.getOldPassword() == null || request.getNewPassword() == null) {
            response.put("success", false);
            response.put("message", "Invalid request body");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String jwt = tokenHeader.startsWith("Bearer ") ? tokenHeader.substring(7) : tokenHeader;
        if (!jwtUtil.validateToken(jwt)) {
            response.put("success", false);
            response.put("message", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String email = jwtUtil.extractEmail(jwt);
        User user = authService.getUserByEmail(email);
        if (user == null) {
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        boolean ok = authService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());
        if (!ok) {
            response.put("success", false);
            response.put("message", "Current password is incorrect");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        response.put("success", true);
        response.put("message", "Password updated successfully");
        return ResponseEntity.ok(response);
    }
}
