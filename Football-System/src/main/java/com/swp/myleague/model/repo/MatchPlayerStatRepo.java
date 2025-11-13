package com.swp.myleague.model.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.entities.match.MatchPlayerStat;

@Repository
public interface MatchPlayerStatRepo extends JpaRepository<MatchPlayerStat, UUID> {

    List<MatchPlayerStat> findAllByPlayerPlayerId(UUID fromString);

    List<MatchPlayerStat> findAllByMatchMatchId(UUID fromString);

    @Query(value = """
                SELECT match_player_stat.* FROM match_player_stat mps
                JOIN player p ON mps.player_id = p.player_id
                WHERE mps.match_id = :matchId AND p.club_id = :clubId
            """, nativeQuery = true)
    List<MatchPlayerStat> findAllByMatchIdAndClubIdNative(
            @Param("matchId") UUID matchId,
            @Param("clubId") UUID clubId);

    List<MatchPlayerStat> findByMatch(Match match);

    void deleteByMatch_MatchId(UUID matchId);


}
