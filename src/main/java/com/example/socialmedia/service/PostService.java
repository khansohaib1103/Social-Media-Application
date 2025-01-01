package com.example.socialmedia.service;

import com.example.socialmedia.ErrorResponse;
import com.example.socialmedia.model.Comment;
import com.example.socialmedia.model.Post;
import com.example.socialmedia.model.User;
import com.example.socialmedia.repository.CommentRepository;
import com.example.socialmedia.repository.PostRepository;
import com.example.socialmedia.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, CommentRepository commentRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    public ResponseEntity<Object> createPost(Post post) {
        try {
            User user = userRepository.findById(post.getUser().getUserID())
                    .orElseThrow(() -> new RuntimeException("User not found!"));
            post.setUser(user);
            post.setTimestamp(LocalDateTime.now());
            Post savedPost = postRepository.save(post);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    public ResponseEntity<Object> getAllPosts(Pageable pageable) {
        try {
            Page<Post> posts = postRepository.findAll(pageable);
            return ResponseEntity.ok(posts.getContent());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("An error occurred while fetching posts"));
        }
    }

    public ResponseEntity<Object> getPostById(Long id) {
        Optional<Post> post = postRepository.findById(id);
        return post.<ResponseEntity<Object>>map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Post not found")));
    }

    public ResponseEntity<Object> updatePost(Long id, Post post) {
        try {
            Post existingPost = postRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Post not found"));
            existingPost.setContent(post.getContent());
            Post updatedPost = postRepository.save(existingPost);
            return ResponseEntity.ok(updatedPost);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    public ResponseEntity<Void> deletePost(Long id) {
        try {
            postRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Object> addComment(Long postId, Comment comment) {
        try {
            Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
            comment.setPost(post);
            comment.setTimestamp(LocalDateTime.now());
            Comment savedComment = commentRepository.save(comment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }

    public ResponseEntity<Object> likePost(Long postId, Long userId) {
        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));
            if (post.getLikes() == null) {
                post.setLikes(new ArrayList<>());
            }
            if (!post.getLikes().contains(userId)) {
                post.getLikes().add(userId);
            }
            Post updatedPost = postRepository.save(post);
            return ResponseEntity.ok(updatedPost);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        }
    }


    public ResponseEntity<Object> searchPosts(String keyword, Pageable pageable) {
        try {
            if (keyword == null || keyword.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Search keyword cannot be blank"));
            }
            Page<Post> posts = postRepository.searchPosts(keyword, pageable);
            if (posts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("No posts found matching the search criteria"));
            }
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Failed to search posts"));
        }
    }
}
