package com.example.socialmedia.service;

import com.example.socialmedia.ErrorResponse;
import com.example.socialmedia.model.Comment;
import com.example.socialmedia.model.Post;
import com.example.socialmedia.model.User;
import com.example.socialmedia.repository.CommentRepository;
import com.example.socialmedia.repository.PostRepository;
import com.example.socialmedia.repository.UserRepository;
import com.example.socialmedia.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPost_Success() {
        User user = new User();
        user.setUserID(1L);

        Post post = new Post();
        post.setUser(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        ResponseEntity<Object> response = postService.createPost(post);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void createPost_UserNotFound() {
        Post post = new Post();
        User user = new User();
        user.setUserID(1L);
        post.setUser(user);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = postService.createPost(post);

        assertEquals(404, response.getStatusCode().value());
        assertInstanceOf(ErrorResponse.class, response.getBody());
    }

    @Test
    void getAllPosts_Success() {
        Post post = new Post();
        Page<Post> page = new PageImpl<>(Collections.singletonList(post));

        when(postRepository.findAll(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Object> response = postService.getAllPosts(Pageable.unpaged());

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void getAllPosts_Exception() {
        Pageable pageable = PageRequest.of(0, 10);
        when(postRepository.findAll(pageable)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<Object> response = postService.getAllPosts(pageable);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("An error occurred while fetching posts", errorResponse.getMessage());
    }


    @Test
    void getPostById_Success() {
        Post post = new Post();
        post.setPostID(1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        ResponseEntity<Object> response = postService.getPostById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(post, response.getBody());
    }

    @Test
    void getPostById_NotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = postService.getPostById(1L);

        assertEquals(404, response.getStatusCode().value());
        assertInstanceOf(ErrorResponse.class, response.getBody());
    }

    @Test
    void updatePost_Success() {
        Post existingPost = new Post();
        existingPost.setPostID(1L);
        existingPost.setContent("Old content");

        Post updatedPost = new Post();
        updatedPost.setContent("New content");

        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(any(Post.class))).thenReturn(updatedPost);

        ResponseEntity<Object> response = postService.updatePost(1L, updatedPost);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(updatedPost.getContent(), ((Post) response.getBody()).getContent());
    }

    @Test
    void updatePost_Exception_PostNotFound() {
        Long postId = 1L;
        Post postToUpdate = new Post();
        postToUpdate.setContent("Updated Content");

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = postService.updatePost(postId, postToUpdate);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Post not found", errorResponse.getMessage());
    }


    @Test
    void deletePost_Success() {
        doNothing().when(postRepository).deleteById(1L);

        ResponseEntity<Void> response = postService.deletePost(1L);

        assertEquals(204, response.getStatusCode().value());
        verify(postRepository, times(1)).deleteById(1L);
    }

    @Test
    void deletePost_Exception() {
        Long postId = 1L;

        doThrow(new RuntimeException("Deletion error")).when(postRepository).deleteById(postId);

        ResponseEntity<Void> response = postService.deletePost(postId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void addComment_Exception_PostNotFound() {
        Long postId = 1L;
        Comment comment = new Comment();
        comment.setContent("This is a comment");

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = postService.addComment(postId, comment);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Post not found", errorResponse.getMessage());
    }



    @Test
    void addComment_Success() {
        Post post = new Post();
        post.setPostID(1L);

        Comment comment = new Comment();
        comment.setContent("This is a comment");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        ResponseEntity<Object> response = postService.addComment(1L, comment);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void likePost_Success() {
        Post post = new Post();
        post.setPostID(1L);
        post.setLikes(new ArrayList<>());

        Long userId = 2L;

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        ResponseEntity<Object> response = postService.likePost(1L, userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((Post) response.getBody()).getLikes().contains(userId));
    }

    @Test
    void likePost_PostNotFound() {
        Long postId = 1L;
        Long userId = 2L;

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = postService.likePost(postId, userId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Post not found", errorResponse.getMessage());
    }



    @Test
    void searchPosts_Success() {
        Post post = new Post();
        Page<Post> page = new PageImpl<>(Collections.singletonList(post));

        when(postRepository.searchPosts(anyString(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Object> response = postService.searchPosts("keyword", Pageable.unpaged());

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void searchPosts_NoPostsFound() {
        String keyword = "exampleKeyword";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> emptyPage = Page.empty();

        when(postRepository.searchPosts(keyword, pageable)).thenReturn(emptyPage);

        ResponseEntity<Object> response = postService.searchPosts(keyword, pageable);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("No posts found matching the search criteria", errorResponse.getMessage());
    }

    @Test
    void searchPosts_Exception() {
        String keyword = "exampleKeyword";
        Pageable pageable = PageRequest.of(0, 10);

        when(postRepository.searchPosts(keyword, pageable)).thenThrow(new RuntimeException("Search error"));

        ResponseEntity<Object> response = postService.searchPosts(keyword, pageable);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Failed to search posts", errorResponse.getMessage());
    }


    @Test
    void searchPosts_KeywordBlank() {
        ResponseEntity<Object> response = postService.searchPosts("", Pageable.unpaged());

        assertEquals(400, response.getStatusCode().value());
        assertInstanceOf(ErrorResponse.class, response.getBody());
    }
}

