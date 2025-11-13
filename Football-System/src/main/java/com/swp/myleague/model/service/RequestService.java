package com.swp.myleague.model.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.admin_request.Request;
import com.swp.myleague.model.repo.RequestRepo;

@Service
public class RequestService implements IService<Request> {

    @Autowired
    RequestRepo requestRepo;

    @Override
    public List<Request> getAll() {
        return requestRepo.findAll();
    }

    @Override
    public Request getById(String id) {
        return requestRepo.findById(UUID.fromString(id)).orElseThrow();
    }

    public List<Request> getByCriteria(Predicate<Request> p) {
        return requestRepo.findAll().stream().filter(p).toList();
    }

    @Override
    public Request save(Request e) {
        if (e.getRequestId() == null) {
            e.setRequestDateCreate(LocalDateTime.now());    
        }
        e.setRequestDateUpdate(LocalDateTime.now());
        return requestRepo.save(e);
    }

    @Override
    public Request delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
    
}
