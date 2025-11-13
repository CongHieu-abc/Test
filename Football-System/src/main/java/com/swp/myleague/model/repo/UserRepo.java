package com.swp.myleague.model.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.User;

@Repository
public interface UserRepo extends JpaRepository<User, UUID>  {

    public Optional<User> findByUsername(String username);

    public boolean existsByUsername(String username);

    public boolean existsByEmail(String email);

    public Optional<User> findByEmail(String email);
    
}
