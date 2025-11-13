package com.swp.myleague.model.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.ClubSubscriber;

@Repository
public interface ClubSubscriberRepo extends JpaRepository<ClubSubscriber, UUID> {

    List<ClubSubscriber> findByClubClubId(UUID clubId);

    Optional<ClubSubscriber> findByEmail(String email);
    
}