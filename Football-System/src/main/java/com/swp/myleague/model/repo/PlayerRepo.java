package com.swp.myleague.model.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.payload.CareerPlayer;
import com.swp.myleague.payload.PlayerStandingAssistDTO;
import com.swp.myleague.payload.PlayerStandingCleanSheetsDTO;
import com.swp.myleague.payload.PlayerStandingGoalDTO;
import com.swp.myleague.payload.PlayerStandingPlayedMinutesDTO;
import com.swp.myleague.payload.TopAppearedClub;
import com.swp.myleague.payload.TopScorerClub;

@Repository
public interface PlayerRepo extends JpaRepository<Player, UUID> {

    List<Player> findAllByClubClubId(UUID fromString);

    Optional<Player> findByPlayerFullName(String playerName);

    // List<Player> getTop10ScorePlayers();
    @Query(value = """
            SELECT BIN_TO_UUID(player_id) AS player_id, SUM(match_player_goal) AS total_goals
            FROM match_player_stat s
            JOIN `'match'` m ON s.match_id = m.match_id
            WHERE YEAR(m.match_start_time) = :season
            AND m.match_start_time < now()
            GROUP BY player_id
            ORDER BY total_goals DESC
            LIMIT 10
            """, nativeQuery = true)
    List<PlayerStandingGoalDTO> findTop10GoalScorersInSeason(@Param("season") Integer season);

    @Query(value = """
            SELECT BIN_TO_UUID(player_id) AS player_id, SUM(match_player_assist) AS total_assists
            FROM match_player_stat s
            JOIN `'match'` m ON s.match_id = m.match_id
            WHERE YEAR(m.match_start_time) = :season
            AND m.match_start_time < now()
            GROUP BY player_id
            ORDER BY total_assists DESC
            LIMIT 10
            """, nativeQuery = true)
    List<PlayerStandingAssistDTO> findTop10AssistersInSeason(@Param("season") Integer season);

    @Query(value = """
            SELECT BIN_TO_UUID(player_id) AS player_id, SUM(match_player_minuted_played) AS total_minutes
            FROM match_player_stat s
            JOIN `'match'` m ON s.match_id = m.match_id
            WHERE YEAR(m.match_start_time) = :season
            AND m.match_start_time < now()
            GROUP BY player_id
            ORDER BY total_minutes DESC
            LIMIT 10
            """, nativeQuery = true)
    List<PlayerStandingPlayedMinutesDTO> findTop10PlayersByMinutesPlayed(@Param("season") Integer season);

    @Query(value = """
                        SELECT
                BIN_TO_UUID(s.player_id) AS player_id,
                COUNT(*) AS clean_sheets
            FROM match_player_stat s
            JOIN `'match'` m ON s.match_id = m.match_id
            JOIN match_club_stat cs ON cs.match_id = m.match_id
            JOIN player p ON p.player_id = s.player_id
            WHERE
                YEAR(m.match_start_time) = :season
                AND m.match_start_time < NOW()
                AND s.is_starter = TRUE
                AND (
                    p.player_position = 'GK'
                    OR (
                        p.player_position = 'CB'
                        AND s.player_id IN (
                            SELECT s2.player_id
                            FROM match_player_stat s2
                            JOIN player p2 ON p2.player_id = s2.player_id
                            WHERE s2.match_id = m.match_id
                              AND p2.club_id = cs.club_id
                        )
                    )
                )
                AND cs.match_club_stat_score = 0
            GROUP BY s.player_id
            ORDER BY clean_sheets DESC
            LIMIT 10;
                        """, nativeQuery = true)
    List<PlayerStandingCleanSheetsDTO> findTop10PlayersByCleanSheets(@Param("season") Integer season);

    @Query(value = """
            SELECT
                p.player_id AS playerId,
                p.player_full_name AS playerFullName,
                p.player_img_path AS playerImgPath,
                p.player_number AS playerNumber,
                p.player_nationaly AS playerNationaly,
                c.club_name AS clubName,
                SUM(s.match_player_goal) AS totalGoals
            FROM match_player_stat s
            JOIN player p ON s.player_id = p.player_id
            JOIN club c ON p.club_id = c.club_id
            WHERE p.club_id = :clubId
            GROUP BY p.player_id, p.player_full_name, p.player_img_path, p.player_number, p.player_nationaly, c.club_name
            ORDER BY totalGoals DESC
            LIMIT 1
            """, nativeQuery = true)
    TopScorerClub getTopScorerDTOByClubId(@Param("clubId") UUID clubId);

    @Query(value = """
            SELECT
                p.player_id AS playerId,
                p.player_full_name AS playerFullName,
                p.player_img_path AS playerImgPath,
                p.player_number AS playerNumber,
                p.player_nationaly AS playerNationaly,
                c.club_name AS clubName,
                COUNT(s.match_id) AS totalAppeared
            FROM match_player_stat s
            JOIN player p ON s.player_id = p.player_id
            JOIN club c ON p.club_id = c.club_id
            WHERE p.club_id = :clubId
            GROUP BY
                p.player_id, p.player_full_name, p.player_img_path,
                p.player_number, p.player_nationaly, c.club_name
            ORDER BY totalAppeared DESC
            LIMIT 1
            """, nativeQuery = true)
    TopAppearedClub getTopAppearedDTOByClubId(@Param("clubId") UUID clubId);

    @Query(value = """
                  SELECT
                ps.player_id AS playerId,
                p.club_id AS clubId,
                c.club_name AS clubName,
                COUNT(ps.match_player_stat_id) AS appeared,
                SUM(ps.match_player_goal) AS goals,
                EXTRACT(YEAR FROM m.match_start_time) AS season
            FROM match_player_stat ps
            JOIN player p ON ps.player_id = p.player_id
            JOIN club c ON p.club_id = c.club_id
            JOIN `'match'` m ON ps.match_id = m.match_id
            WHERE ps.player_id = :playerId
            GROUP BY p.club_id, ps.player_id,c.club_name, EXTRACT(YEAR FROM m.match_start_time)
            ORDER BY season;
                  """, nativeQuery = true)
    List<CareerPlayer> findCareerPlayerByPlayerId(@Param("playerId") UUID playerId);

}
