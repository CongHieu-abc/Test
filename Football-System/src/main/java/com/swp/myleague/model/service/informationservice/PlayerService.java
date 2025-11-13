package com.swp.myleague.model.service.informationservice;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.model.repo.PlayerRepo;
import com.swp.myleague.payload.CareerPlayer;
import com.swp.myleague.payload.PlayerStandingAssistDTO;
import com.swp.myleague.payload.PlayerStandingCleanSheetsDTO;
import com.swp.myleague.payload.PlayerStandingGoalDTO;
import com.swp.myleague.payload.PlayerStandingPlayedMinutesDTO;
import com.swp.myleague.payload.TopAppearedClub;
import com.swp.myleague.payload.TopScorerClub;

@Service
public class PlayerService implements IService<Player> {

    @Autowired
    PlayerRepo playerRepo;

    @Override
    public List<Player> getAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }

    public List<Player> getPlayersByClubId(String clubId) {
        return playerRepo.findAllByClubClubId(UUID.fromString(clubId));
    }

    // public List<Player> getTop10ScorePlayers() {
    // return playerRepo.getTop10ScorePlayers();
    // }

    @Override
    public Player getById(String id) {
        return playerRepo.findById(UUID.fromString(id)).orElseThrow();
    }

    @Override
    public Player save(Player e) {
        return playerRepo.save(e);
    }

    @Override
    public Player delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

public Map<Player, PlayerStandingGoalDTO> getTop10PlayerGoalByYear(String year) {
        List<PlayerStandingGoalDTO> topScorers = playerRepo.findTop10GoalScorersInSeason(Integer.parseInt(year));

        Map<UUID, PlayerStandingGoalDTO> dtoMap = topScorers.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getPlayerId(),
                        dto -> dto));

        List<Player> players = playerRepo.findAllById(dtoMap.keySet());

        Map<Player, PlayerStandingGoalDTO> result = new LinkedHashMap<>();
        for (Player player : players) {
            PlayerStandingGoalDTO dto = dtoMap.get(player.getPlayerId());
            if (dto != null) {
                result.put(player, dto);
            }
        }

        Map<Player, PlayerStandingGoalDTO> sortedResult = result.entrySet().stream()
                .sorted(Map.Entry.<Player, PlayerStandingGoalDTO>comparingByValue(
                        Comparator.comparing(dto -> {
                            try {
                                return Integer.parseInt(dto.getTotalGoals());
                            } catch (NumberFormatException e) {
                                return 0; // Default value if parsing fails
                            }
                        })).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        return sortedResult;
    }

    public Map<Player, PlayerStandingAssistDTO> getTop10PlayerAssistByYear(String year) {
        List<PlayerStandingAssistDTO> topAssist = playerRepo.findTop10AssistersInSeason(Integer.parseInt(year));

        Map<UUID, PlayerStandingAssistDTO> dtoMap = topAssist.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getPlayerId(),
                        dto -> dto));

        List<Player> players = playerRepo.findAllById(dtoMap.keySet());

        Map<Player, PlayerStandingAssistDTO> result = new LinkedHashMap<>();
        for (Player player : players) {
            PlayerStandingAssistDTO dto = dtoMap.get(player.getPlayerId());
            if (dto != null) {
                result.put(player, dto);
            }
        }

        Map<Player, PlayerStandingAssistDTO> sortedResult = result.entrySet().stream()
                .sorted(Map.Entry.<Player, PlayerStandingAssistDTO>comparingByValue(
                        Comparator.comparing(dto -> {
                            try {
                                return Integer.parseInt(dto.getTotalAssists());
                            } catch (NumberFormatException e) {
                                return 0; // Default value if parsing fails
                            }
                        })).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        return sortedResult;

    }

    public Map<Player, PlayerStandingCleanSheetsDTO> getTop10PlayerCleanSheetByYear(String year) {
        List<PlayerStandingCleanSheetsDTO> topCleansheet = playerRepo
                .findTop10PlayersByCleanSheets(Integer.parseInt(year));

        Map<UUID, PlayerStandingCleanSheetsDTO> dtoMap = topCleansheet.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getPlayerId(),
                        dto -> dto));

        List<Player> players = playerRepo.findAllById(dtoMap.keySet());

        Map<Player, PlayerStandingCleanSheetsDTO> result = new LinkedHashMap<>();
        for (Player player : players) {
            PlayerStandingCleanSheetsDTO dto = dtoMap.get(player.getPlayerId());
            if (dto != null) {
                result.put(player, dto);
            }
        }

        Map<Player, PlayerStandingCleanSheetsDTO> sortedResult = result.entrySet().stream()
                .sorted(Map.Entry.<Player, PlayerStandingCleanSheetsDTO>comparingByValue(
                        Comparator.comparing(dto -> {
                            try {
                                return Integer.parseInt(dto.getCleanSheets());
                            } catch (NumberFormatException e) {
                                return 0; // Default value if parsing fails
                            }
                        })).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        return sortedResult;
    }

    public Map<Player, PlayerStandingPlayedMinutesDTO> getTop10PlayerMinutePlayedByYear(String year) {
        List<PlayerStandingPlayedMinutesDTO> topPlayedMinutes = playerRepo
                .findTop10PlayersByMinutesPlayed(Integer.parseInt(year));

        Map<UUID, PlayerStandingPlayedMinutesDTO> dtoMap = topPlayedMinutes.stream()
                .collect(Collectors.toMap(
                        dto -> dto.getPlayerId(),
                        dto -> dto));

        List<Player> players = playerRepo.findAllById(dtoMap.keySet());

        Map<Player, PlayerStandingPlayedMinutesDTO> result = new LinkedHashMap<>();
        for (Player player : players) {
            PlayerStandingPlayedMinutesDTO dto = dtoMap.get(player.getPlayerId());
            if (dto != null) {
                result.put(player, dto);
            }
        }

        Map<Player, PlayerStandingPlayedMinutesDTO> sortedResult = result.entrySet().stream()
                .sorted(Map.Entry.<Player, PlayerStandingPlayedMinutesDTO>comparingByValue(
                        Comparator.comparing(dto -> {
                            try {
                                return Integer.parseInt(dto.getTotalMinutes());
                            } catch (NumberFormatException e) {
                                return 0; // Default value if parsing fails
                            }
                        })).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        return sortedResult;
    }

    public TopScorerClub getTopScorerOfClub(String clubId) {
        return playerRepo.getTopScorerDTOByClubId(UUID.fromString(clubId));
    }

    public TopAppearedClub getTopAppearedOfClub(String clubId) {
        return playerRepo.getTopAppearedDTOByClubId(UUID.fromString(clubId));
    }

    public List<CareerPlayer> getCareerByPlayerId(String playerId) {
        return playerRepo.findCareerPlayerByPlayerId(UUID.fromString(playerId));
    }

}
