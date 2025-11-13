package com.swp.myleague.model.service.blogservice;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.blog.Blog;
import com.swp.myleague.model.repo.BlogRepo;

@Service
public class BlogService implements IService<Blog> {

    @Autowired BlogRepo blogRepo;

    @Override
    public List<Blog> getAll() {
        return blogRepo.findAll();
    }

    public List<Blog> getByClubId(String clubId) {
        return blogRepo.findAllByClubClubId(UUID.fromString(clubId));
    }

    @Override
    public Blog getById(String id) {
        return blogRepo.findById(UUID.fromString(id)).orElseThrow();
    }

    @Override
    public Blog save(Blog e) {
        return blogRepo.save(e);
    }

    @Override
    public Blog delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
    


}
