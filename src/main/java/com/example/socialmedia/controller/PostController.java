package com.example.socialmedia.controller;

import com.example.socialmedia.model.Comment;
import com.example.socialmedia.model.Post;
import com.example.socialmedia.service.PostService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@Tag(name = "Posts", description = "Endpoints for managing posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Operation(summary = "Create a new post", description = "Adds a new post to the platform")
    @ApiResponse(responseCode = "201", description = "Post created successfully")
    @PostMapping
    public ResponseEntity<Object> createPost(@RequestBody Post post) {
        return postService.createPost(post);
    }

    @Operation(summary = "Get all posts", description = "Retrieves all posts with pagination")
    @ApiResponse(responseCode = "200", description = "Posts retrieved successfully")
    @GetMapping
    public ResponseEntity<Object> getAllPosts(Pageable pageable) {
        return postService.getAllPosts(pageable);
    }

    @Operation(summary = "Get a post by ID", description = "Retrieves a specific post by ID")
    @ApiResponse(responseCode = "200", description = "Post retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Post not found")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getPost(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    @Operation(summary = "Update a post", description = "Updates the details of an existing post")
    @ApiResponse(responseCode = "200", description = "Post updated successfully")
    @ApiResponse(responseCode = "404", description = "Post not found")
    @PutMapping("/{id}")
    public ResponseEntity<Object> updatePost(@PathVariable Long id, @RequestBody Post post) {
        return postService.updatePost(id, post);
    }

    @Operation(summary = "Delete a post", description = "Deletes a specific post by ID")
    @ApiResponse(responseCode = "204", description = "Post deleted successfully")
    @ApiResponse(responseCode = "404", description = "Post not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        return postService.deletePost(id);
    }

    @Operation(summary = "Add a comment", description = "Adds a comment to a specific post")
    @ApiResponse(responseCode = "201", description = "Comment added successfully")
    @PostMapping("/{id}/comments")
    public ResponseEntity<Object> addComment(@PathVariable Long id, @RequestBody Comment comment) {
        return postService.addComment(id, comment);
    }

    @Operation(summary = "Like a post", description = "Likes a specific post by ID")
    @ApiResponse(responseCode = "200", description = "Post liked successfully")
    @PostMapping("/{id}/like")
    public ResponseEntity<Object> likePost(@PathVariable Long id, @RequestBody Long userId) {
        return postService.likePost(id, userId);
    }

    @Operation(summary = "Search posts", description = "Searches posts by keyword with pagination")
    @ApiResponse(responseCode = "200", description = "Posts retrieved successfully")
    @PostMapping("/search")
    public ResponseEntity<Object> searchPosts(@RequestParam String keyword, Pageable pageable) {
        return postService.searchPosts(keyword, pageable);
    }
}