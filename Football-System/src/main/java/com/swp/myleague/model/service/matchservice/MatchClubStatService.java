package com.swp.myleague.model.service.matchservice;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.match.MatchClubStat;
import com.swp.myleague.model.repo.ClubRepo;
import com.swp.myleague.model.repo.MatchClubStatRepo;
import com.swp.myleague.payload.ClubStandingDTO;

@Service
public class MatchClubStatService implements IService<MatchClubStat> {

    @Autowired
    MatchClubStatRepo matchClubStatRepo;

    @Autowired
    ClubRepo clubRepo;

    @Override
    public List<MatchClubStat> getAll() {
        return matchClubStatRepo.findAll();
    }

    public List<MatchClubStat> getAllByMatchId(String id) throws Exception {
        List<MatchClubStat> matchClubStats = matchClubStatRepo.findAllByMatchMatchId(UUID.fromString(id));
        if (matchClubStats == null || matchClubStats.isEmpty()) {
            throw new Exception("Cannot load data with id: " + UUID.fromString(id));
        }
        return matchClubStats;
    }

    public List<MatchClubStat> getAllByClubId(String id) throws Exception {
        List<MatchClubStat> matchClubStats = matchClubStatRepo.findAllByClubClubId(UUID.fromString(id));
        if (matchClubStats == null || matchClubStats.isEmpty()) {
            throw new Exception("Cannot load data with id: " + UUID.fromString(id));
        }
        return matchClubStats;
    }

    public List<ClubStandingDTO> getClubStandings(Integer season) {
        return matchClubStatRepo.findClubStandingsBySeason(season);
    }

    

    @Override
    public MatchClubStat getById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }

    @Override
    public MatchClubStat save(MatchClubStat e) {
        return matchClubStatRepo.save(e);
    }

    @Override
    public MatchClubStat delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}
