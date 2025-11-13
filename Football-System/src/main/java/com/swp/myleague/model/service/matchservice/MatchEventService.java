package com.swp.myleague.model.service.matchservice;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.match.MatchEvent;
import com.swp.myleague.model.entities.match.MatchEventType;
import com.swp.myleague.model.repo.MatchEventRepo;

@Service
public class MatchEventService implements IService<MatchEvent> {

    @Autowired
    MatchEventRepo matchEventRepo;

    @Override
    public List<MatchEvent> getAll() {
        return matchEventRepo.findAll();
    }

    public List<MatchEvent> getAllByMatchId(String matchId) {
        return matchEventRepo.findAllByMatchMatchId(UUID.fromString(matchId));
    }

    @Override
    public MatchEvent getById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }

    @Override
    public MatchEvent save(MatchEvent e) {
        return matchEventRepo.save(e);
    }

    @Override
    public MatchEvent delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    public List<MatchEvent> getAllHighlightByClubId(String clubId) {
        List<MatchEvent> highligMatchEvents = getAll().stream()
        .filter(me -> me.getMatchEventType() == MatchEventType.Highlight
                && me.getMatch() != null
                && me.getMatch().getMatchClubStats() != null
                && me.getMatch().getMatchClubStats().stream()
                        .anyMatch(stat -> stat.getClub() != null && clubId.equals(stat.getClub().getClubId().toString())))
        .toList();
        return highligMatchEvents;
    }

}
