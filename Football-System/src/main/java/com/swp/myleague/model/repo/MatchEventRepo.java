package com.swp.myleague.model.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.match.MatchEvent;

@Repository
public interface MatchEventRepo extends JpaRepository<MatchEvent, UUID> {

    List<MatchEvent> findAllByMatchMatchId(UUID fromString);
    
}
