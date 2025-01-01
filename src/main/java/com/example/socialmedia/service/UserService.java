package com.example.socialmedia.service;

import com.example.socialmedia.ErrorResponse;
import com.example.socialmedia.model.Follow;
import com.example.socialmedia.model.LoginUser;
import com.example.socialmedia.model.User;
import com.example.socialmedia.repository.FollowRepository;
import com.example.socialmedia.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    public UserService(UserRepository userRepository, FollowRepository followRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public ResponseEntity<Object> registerUser(User user) {
        try {
            if (userRepository.existsByUsername(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Username already exists"));
            }
            if (user.getUsername() == null || user.getUsername().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Username cannot be blank"));
            }
            if (user.getPassword().length() < 6) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Password must be at least 6 characters"));
            }

            String password = user.getPassword();
            String encodedPassword = passwordEncoder.encode(password);
            user.setPassword(encodedPassword);
            User savedUser = userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An error occurred while registering the user"));
        }
    }

    public User authenticate(LoginUser input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUsername(),
                        input.getPassword()
                )
        );

        return userRepository.findByUsername(input.getUsername())
                .orElseThrow();
    }

    public ResponseEntity<Object> getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);

        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("User not found"));
        }
    }

    public ResponseEntity<Object> getFollowers(Long userId) {
        try {
            List<User> followers = followRepository.findFollowersByUserId(userId);
            if (followers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("No followers found"));
            }
            return ResponseEntity.ok(followers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Failed to fetch followers"));
        }
    }

    public ResponseEntity<Object> getFollowing(Long userId) {
        try {
            List<User> following = followRepository.findFollowingByUserId(userId);
            if (following.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("No users are being followed"));
            }
            return ResponseEntity.ok(following);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Failed to fetch following users"));
        }
    }

    public ResponseEntity<Object> followUser(Long followerId, Long followingId) {
        try {
            if (followerId.equals(followingId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("You cannot follow yourself"));
            }

            User follower = userRepository.findById(followerId)
                    .orElseThrow(() -> new RuntimeException("Follower not found"));

            User following = userRepository.findById(followingId)
                    .orElseThrow(() -> new RuntimeException("Following user not found"));

            if (followRepository.existsByFollowerAndFollowing(follower, following)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("You are already following this user"));
            }

            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowing(following);
            Follow savedFollow = followRepository.save(follow);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFollow);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Failed to follow user"));
        }
    }

    public ResponseEntity<Object> searchUsers(String keyword, Pageable pageable) {
        try {
            if (keyword == null || keyword.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Search keyword cannot be blank"));
            }
            Page<User> users = userRepository.searchUsers(keyword, pageable);
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("No users found matching the search criteria"));
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Failed to search users"));
        }
    }
}
