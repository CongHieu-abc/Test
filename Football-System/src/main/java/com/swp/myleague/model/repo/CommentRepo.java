package com.swp.myleague.model.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.Comment;

@Repository
public interface CommentRepo extends JpaRepository<Comment, UUID> {

    List<Comment> findAllByBlogBlogId(UUID fromString);

    List<Comment> findAllByMatchMatchId(UUID fromString);
    
}
