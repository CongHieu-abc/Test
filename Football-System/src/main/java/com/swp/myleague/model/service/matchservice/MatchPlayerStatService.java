package com.swp.myleague.model.service.matchservice;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.model.entities.match.MatchPlayerStat;
import com.swp.myleague.model.repo.MatchPlayerStatRepo;
import com.swp.myleague.model.repo.PlayerRepo;

@Service
public class MatchPlayerStatService implements IService<MatchPlayerStat> {

    @Autowired
    MatchPlayerStatRepo matchPlayerStatRepo;

    @Autowired PlayerRepo playerRepo;

    @Override
    public List<MatchPlayerStat> getAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }

    public List<MatchPlayerStat> getAllByPlayerId(String playerId) {
        return matchPlayerStatRepo.findAllByPlayerPlayerId(UUID.fromString(playerId));
    }

    public List<MatchPlayerStat> getAllByMatchId(String matchId) {
        return matchPlayerStatRepo.findAllByMatchMatchId(UUID.fromString(matchId));
    }

    public MatchPlayerStat getAllByPlayerAndMatch(String playerId, String matchId) {
        UUID playerUUID = UUID.fromString(playerId);
        UUID matchUUID = UUID.fromString(matchId);

        return matchPlayerStatRepo.findAllByPlayerPlayerId(playerUUID).stream()
                .filter(mps -> mps.getMatch().getMatchId().equals(matchUUID))
                .findFirst() // Lấy phần tử đầu tiên phù hợp
                .orElse(null); // Trả về null nếu không tìm thấy
    }

    @Override
    public MatchPlayerStat getById(String id) {
        return matchPlayerStatRepo.findById(UUID.fromString(id)).orElseThrow();
    }

    @Override
    public MatchPlayerStat save(MatchPlayerStat e) {
        Player player = playerRepo.findById(e.getPlayer().getPlayerId()).orElseThrow();
        player.setPlayerAppearances(player.getPlayerAppearances() + 1);
        player.setPlayerAssist(player.getPlayerAssist() + e.getMatchPlayerAssist());
        player.setPlayerScores(player.getPlayerScores() + e.getMatchPlayerGoal());
        playerRepo.save(player);
        return matchPlayerStatRepo.save(e);
    }

    @Override
    public MatchPlayerStat delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    public List<MatchPlayerStat> getAllByMatchIdandClubId(String matchId, String clubId) {
        return matchPlayerStatRepo.findAllByMatchIdAndClubIdNative(UUID.fromString(matchId), UUID.fromString(clubId));
    }

    public void deleteByMatchId(UUID matchId) {
    matchPlayerStatRepo.deleteByMatch_MatchId(matchId);
}


}
