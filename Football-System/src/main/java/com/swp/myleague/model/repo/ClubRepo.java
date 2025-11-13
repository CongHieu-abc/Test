package com.swp.myleague.model.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.information.Club;
import com.swp.myleague.payload.ClubStatOverview;

@Repository
public interface ClubRepo extends JpaRepository<Club, UUID> {

    Club findByUserId(UUID fromString);

    @Query(value = """
                    SELECT
                c.club_id AS clubId,
                COALESCE(SUM(mcs.match_club_stat_score), 0) AS allGoals,
                COALESCE(SUM(mps.match_player_assist), 0) AS allAssists
            FROM club c
            LEFT JOIN match_club_stat mcs ON c.club_id = mcs.club_id
            LEFT JOIN `'match'` m ON mcs.match_id = m.match_id
            LEFT JOIN player p ON c.club_id = p.club_id
            LEFT JOIN match_player_stat mps ON p.player_id = mps.player_id AND mps.match_id = m.match_id
            WHERE c.club_id = :clubId
              AND EXTRACT(YEAR FROM m.match_start_time) = :season
              AND is_active = true
            GROUP BY c.club_id
                    """, nativeQuery = true)
    ClubStatOverview getClubStatOverview(@Param("clubId") UUID clubId, @Param("season") Integer season);

}
