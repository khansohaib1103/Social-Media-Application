package com.example.socialmedia.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long PostID;

    @ManyToOne
    private User user;

    private String content;
    private LocalDateTime timestamp;

    @OneToMany(mappedBy = "post")
    private List<Comment> comments;

    @ElementCollection
    private List<Long> likes;
}
