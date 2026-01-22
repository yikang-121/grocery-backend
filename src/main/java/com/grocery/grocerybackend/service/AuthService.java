package com.grocery.grocerybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grocery.grocerybackend.dto.ChangePasswordRequest;
import com.grocery.grocerybackend.entity.User;
import com.grocery.grocerybackend.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public boolean register(User user) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("email", user.getEmail());

        if (userMapper.selectCount(query) > 0) return false;

        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole("user"); // default role for new registrations
        user.setCreated_at(LocalDateTime.now());

        return userMapper.insert(user) == 1;
    }

    public User login(String email, String password) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", email));
        if (user != null && encoder.matches(password, user.getPassword())) {
            // Return user with role information intact, but hide password
            User loginUser = new User();
            loginUser.setId(user.getId());
            loginUser.setEmail(user.getEmail());
            loginUser.setName(user.getName());
            loginUser.setRole(user.getRole()); // Include role in response
            loginUser.setCreated_at(user.getCreated_at());
            return loginUser;
        }
        return null;
    }

    public User getUserByEmail(String email) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("email", email);
        User user = userMapper.selectOne(query);

        if (user != null) {
            user.setPassword(null); // Hide password
        }

        return user;
    }

    public boolean isAdmin(String email) {
        User user = getUserByEmail(email);
        return user != null && "admin".equals(user.getRole());
    }

    // Method to manually create admin users (you can call this from a separate endpoint or script)
    public boolean createAdminUser(String email, String password, String name) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("email", email);

        if (userMapper.selectCount(query) > 0) return false;

        User adminUser = new User();
        adminUser.setEmail(email);
        adminUser.setPassword(encoder.encode(password));
        adminUser.setName(name);
        adminUser.setRole("admin"); // Set as admin
        adminUser.setCreated_at(LocalDateTime.now());

        return userMapper.insert(adminUser) == 1;
    }

    /* ---------------------- New: Change password ---------------------- */

    @Transactional
    public void changePasswordByUserId(Long userId, ChangePasswordRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new IllegalArgumentException("User not found");

        // verify current password
        if (!encoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // simple policy (extend as you wish)
        String np = req.getNewPassword();
        if (np.length() < 8 || np.length() > 128) {
            throw new IllegalArgumentException("Password must be 8–128 characters");
        }
        if (encoder.matches(np, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from the current one");
        }

        String newHash = encoder.encode(np);
        int rows = userMapper.updatePassword(user.getId(), newHash);
        if (rows == 0) throw new IllegalStateException("Failed to update password");
    }

    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null || oldPassword == null || newPassword == null || newPassword.isBlank()) {
            return false;
        }
        User user = userMapper.selectById(userId);
        if (user == null) return false;

        if (!encoder.matches(oldPassword, user.getPassword())) {
            return false; // current password incorrect
        }

        user.setPassword(encoder.encode(newPassword));
        return userMapper.updateById(user) == 1;
    }
}