package com.example.socialmedia.service;

import com.example.socialmedia.ErrorResponse;
import com.example.socialmedia.model.Follow;
import com.example.socialmedia.model.LoginUser;
import com.example.socialmedia.model.User;
import com.example.socialmedia.repository.FollowRepository;
import com.example.socialmedia.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_Success() {
        User user = new User();
        user.setUsername("newUser");
        user.setPassword("password123");

        when(userRepository.existsByUsername(user.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<Object> response = userService.registerUser(user);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        User savedUser = (User) response.getBody();
        assertEquals("encodedPassword", savedUser.getPassword());

        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }


    @Test
    void registerUser_UsernameConflict() {
        User user = new User();
        user.setUsername("existingUser");
        user.setPassword("password");

        when(userRepository.existsByUsername(user.getUsername())).thenReturn(true);

        ResponseEntity<Object> response = userService.registerUser(user);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Assertions.assertEquals("Username already exists", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void registerUser_BlankUsername() {
        User user = new User();
        user.setUsername("");
        user.setPassword("password");

        ResponseEntity<Object> response = userService.registerUser(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username cannot be blank", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void registerUser_ShortPassword() {
        User user = new User();
        user.setUsername("newUser");
        user.setPassword("123");

        ResponseEntity<Object> response = userService.registerUser(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Password must be at least 6 characters", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void registerUser_InternalError() {
        User user = new User();
        user.setUsername("newUser");
        user.setPassword("validPassword");

        when(userRepository.existsByUsername(user.getUsername())).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<Object> response = userService.registerUser(user);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred while registering the user", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void authenticate_Success() {
        LoginUser input = new LoginUser("testUser", "password");
        User user = new User();
        user.setUsername("testUser");

        when(userRepository.findByUsername(input.getUsername())).thenReturn(Optional.of(user));

        User result = userService.authenticate(input);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
    }

    @Test
    void getUserById_UserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = userService.getUserById(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void followUser_Success() {
        Long followerId = 1L;
        Long followingId = 2L;

        User follower = new User();
        User following = new User();

        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followingId)).thenReturn(Optional.of(following));
        when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(false);
        when(followRepository.save(any(Follow.class))).thenReturn(new Follow());

        ResponseEntity<Object> response = userService.followUser(followerId, followingId);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getFollowers_NoFollowers() {
        Long userId = 1L;

        when(followRepository.findFollowersByUserId(userId)).thenReturn(List.of());

        ResponseEntity<Object> response = userService.getFollowers(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No followers found", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void getFollowers_FetchError() {
        Long userId = 1L;

        when(followRepository.findFollowersByUserId(userId)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<Object> response = userService.getFollowers(userId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to fetch followers", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void followUser_SelfFollow() {
        Long followerId = 1L;
        Long followingId = 1L;

        ResponseEntity<Object> response = userService.followUser(followerId, followingId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("You cannot follow yourself", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void followUser_FollowerNotFound() {
        Long followerId = 1L;
        Long followingId = 2L;

        when(userRepository.findById(followerId)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = userService.followUser(followerId, followingId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to follow user", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void getFollowing_Success() {
        Long userId = 1L;
        User user1 = new User();
        User user2 = new User();
        List<User> following = List.of(user1, user2);

        when(followRepository.findFollowingByUserId(userId)).thenReturn(following);

        ResponseEntity<Object> response = userService.getFollowing(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(following, response.getBody());
    }

    @Test
    void getFollowing_NotFound() {
        Long userId = 1L;

        when(followRepository.findFollowingByUserId(userId)).thenReturn(Collections.emptyList());

        ResponseEntity<Object> response = userService.getFollowing(userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No users are being followed", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void getFollowing_InternalServerError() {
        Long userId = 1L;

        when(followRepository.findFollowingByUserId(userId)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<Object> response = userService.getFollowing(userId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to fetch following users", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void followUser_FollowingNotFound() {
        Long followerId = 1L;
        Long followingId = 2L;

        when(userRepository.findById(followerId)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(followingId)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = userService.followUser(followerId, followingId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to follow user", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void followUser_AlreadyFollowing() {
        Long followerId = 1L;
        Long followingId = 2L;

        User follower = new User();
        User following = new User();

        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(followingId)).thenReturn(Optional.of(following));
        when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(true);

        ResponseEntity<Object> response = userService.followUser(followerId, followingId);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("You are already following this user", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void followUser_Exception() {
        Long followerId = 1L;
        Long followingId = 2L;

        when(userRepository.findById(followerId)).thenReturn(Optional.of(new User()));
        when(userRepository.findById(followingId)).thenReturn(Optional.of(new User()));
        when(followRepository.existsByFollowerAndFollowing(any(), any())).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<Object> response = userService.followUser(followerId, followingId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to follow user", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void searchUsers_BlankKeyword() {
        String keyword = "";

        ResponseEntity<Object> response = userService.searchUsers(keyword, Pageable.unpaged());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Search keyword cannot be blank", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void searchUsers_NoUsersFound() {
        String keyword = "nonexistent";

        Page<User> emptyPage = Page.empty();
        when(userRepository.searchUsers(keyword, Pageable.unpaged())).thenReturn(emptyPage);

        ResponseEntity<Object> response = userService.searchUsers(keyword, Pageable.unpaged());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No users found matching the search criteria", ((ErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void searchUsers_Exception() {
        String keyword = "test";

        when(userRepository.searchUsers(keyword, Pageable.unpaged())).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<Object> response = userService.searchUsers(keyword, Pageable.unpaged());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to search users", ((ErrorResponse) response.getBody()).getMessage());
    }
}

