package com.swp.myleague.model.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.Comment;
import com.swp.myleague.model.repo.CommentRepo;

@Service
public class CommentService implements IService<Comment> {

    @Autowired
    CommentRepo commentRepo;

    @Override
    public List<Comment> getAll() {
        return commentRepo.findAll();
    }

    @Override
    public Comment getById(String id) {
        return commentRepo.findById(UUID.fromString(id)).orElseThrow();
    }

    @Override
    public Comment save(Comment e) {
        e.setCommentDateCreated(LocalDateTime.now());
        return commentRepo.save(e);
    }

    @Override
    public Comment delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    public List<Comment> getAllCommentsByBlogId(String blogId) {
        return commentRepo.findAllByBlogBlogId(UUID.fromString(blogId));
    }

    public List<Comment> getAllCommentsByMatchId(String matchId) {
        return commentRepo.findAllByMatchMatchId(UUID.fromString(matchId));
    }
    
}
