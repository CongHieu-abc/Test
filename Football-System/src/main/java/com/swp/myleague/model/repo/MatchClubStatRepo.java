package com.swp.myleague.model.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.match.MatchClubStat;
import com.swp.myleague.payload.ClubStandingDTO;

@Repository
public interface MatchClubStatRepo extends JpaRepository<MatchClubStat, UUID> {

    public List<MatchClubStat> findAllByMatchMatchId(UUID matchId);

    public List<MatchClubStat> findAllByClubClubId(UUID fromString);

    @Query(value = """
                                        SELECT
                c.club_name AS clubName,
                SUM(
                    CASE
                        WHEN m.match_start_time <= NOW()
                        AND mcs.match_club_stat_score > opp.match_club_stat_score THEN 3
                        WHEN m.match_start_time <= NOW()
                        AND mcs.match_club_stat_score = opp.match_club_stat_score THEN 1
                        ELSE 0
                    END
                ) AS points,
                c.club_logo_path AS logoUrl,
                COUNT(CASE WHEN m.match_start_time <= NOW() THEN mcs.match_id END) AS played,
                SUM(CASE WHEN m.match_start_time <= NOW() AND mcs.match_club_stat_score > opp.match_club_stat_score THEN 1 ELSE 0 END) AS wins,
                SUM(CASE WHEN m.match_start_time <= NOW() AND mcs.match_club_stat_score = opp.match_club_stat_score THEN 1 ELSE 0 END) AS draws,
                SUM(CASE WHEN m.match_start_time <= NOW() AND mcs.match_club_stat_score < opp.match_club_stat_score THEN 1 ELSE 0 END) AS losses,
                SUM(CASE WHEN m.match_start_time <= NOW() THEN mcs.match_club_stat_score ELSE 0 END) AS goalsFor,
                SUM(CASE WHEN m.match_start_time <= NOW() THEN opp.match_club_stat_score ELSE 0 END) AS goalsAgainst,
                SUM(CASE WHEN m.match_start_time <= NOW() THEN mcs.match_club_stat_score - opp.match_club_stat_score ELSE 0 END) AS goalDifference,

                -- NEXT OPPONENT LOGO
                (
                    SELECT c2.club_logo_path
                    FROM match_club_stat mcs2
                    JOIN `'match'` m2 ON m2.match_id = mcs2.match_id
                    JOIN match_club_stat mcs_opp ON mcs_opp.match_id = mcs2.match_id AND mcs_opp.club_id != mcs2.club_id
                    JOIN club c2 ON c2.club_id = mcs_opp.club_id
                    WHERE mcs2.club_id = mcs.club_id
                      AND m2.match_start_time > NOW()
                    ORDER BY m2.match_start_time ASC
                    LIMIT 1
                ) AS nextLogoUrl

            FROM match_club_stat mcs
            JOIN `'match'` m ON m.match_id = mcs.match_id
            JOIN club c ON c.club_id = mcs.club_id
            JOIN match_club_stat opp ON opp.match_id = mcs.match_id AND opp.club_id != mcs.club_id
            WHERE YEAR(m.match_start_time) = :season
              AND c.is_active = true
            GROUP BY c.club_id, c.club_name, c.club_logo_path
            ORDER BY points DESC, goalDifference DESC, goalsFor DESC;

                                    """, nativeQuery = true)
    List<ClubStandingDTO> findClubStandingsBySeason(@Param("season") Integer season);

}
