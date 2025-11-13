package com.swp.myleague.model.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.swp.myleague.model.entities.blog.Blog;
import com.swp.myleague.model.entities.match.Match;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Comment {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID commentId;

    private String commentContent;
    private LocalDateTime commentDateCreated;
    
    @ManyToOne
    @JoinColumn(name = "commentCreatedBy")
    private User user;

    @ManyToOne
    @JoinColumn(name = "blogId")
    private Blog blog;

    @ManyToOne
    @JoinColumn(name = "matchId")
    private Match match;

}
