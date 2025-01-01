package com.example.socialmedia.controller;

import com.example.socialmedia.jwt.JwtService;
import com.example.socialmedia.model.LoginResponse;
import com.example.socialmedia.model.LoginUser;
import com.example.socialmedia.model.User;
import com.example.socialmedia.service.UserService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Endpoints for managing users")
class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    @Operation(summary = "Register a new user", description = "Registers a new user account")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @Operation(summary = "Login user", description = "Authenticates a user and returns a JWT token")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUser loginUser) {
        User authenticatedUser = userService.authenticate(loginUser);
        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by ID")
    @ApiResponse(responseCode = "200", description = "User retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "Follow a user", description = "Allows a user to follow another user")
    @ApiResponse(responseCode = "200", description = "Followed successfully")
    @PostMapping("/{id}/{followingId}/follow")
    public ResponseEntity<Object> followUser(@PathVariable Long id, @PathVariable Long followingId) {
        return userService.followUser(id, followingId);
    }

    @Operation(summary = "Get followers", description = "Retrieves the list of followers of a user")
    @ApiResponse(responseCode = "200", description = "Followers retrieved successfully")
    @GetMapping("/{id}/followers")
    public ResponseEntity<Object> getFollowers(@PathVariable Long id) {
        return userService.getFollowers(id);
    }

    @Operation(summary = "Get following", description = "Retrieves the list of users a user is following")
    @ApiResponse(responseCode = "200", description = "Following retrieved successfully")
    @GetMapping("/{id}/following")
    public ResponseEntity<Object> getFollowing(@PathVariable Long id) {
        return userService.getFollowing(id);
    }

    @Operation(summary = "Search users", description = "Searches users by keyword with pagination")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @PostMapping("/search")
    public ResponseEntity<Object> searchUsers(@RequestParam String keyword, Pageable pageable) {
        return userService.searchUsers(keyword, pageable);
    }
}
