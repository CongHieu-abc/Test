package com.swp.myleague.model.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.blog.Blog;

@Repository
public interface BlogRepo extends JpaRepository<Blog, UUID> {

    List<Blog> findAllByClubClubId(UUID fromString);
    
}
