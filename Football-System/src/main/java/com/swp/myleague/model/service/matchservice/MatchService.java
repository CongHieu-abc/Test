package com.swp.myleague.model.service.matchservice;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swp.myleague.common.IService;
import com.swp.myleague.model.entities.information.Club;
import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.entities.match.MatchClubStat;
import com.swp.myleague.model.entities.match.MatchPlayerStat;
import com.swp.myleague.model.repo.ClubRepo;
import com.swp.myleague.model.repo.MatchClubStatRepo;
import com.swp.myleague.model.repo.MatchRepo;

@Service
public class MatchService implements IService<Match> {

    @Autowired
    MatchRepo matchRepo;

    @Autowired
    ClubRepo clubRepo;

    @Autowired
    MatchClubStatRepo matchClubStatRepo;

    @Override
    public List<Match> getAll() {
        return matchRepo.findAll();
    }

    @Override
    public Match getById(String id) {
        return matchRepo.findById(UUID.fromString(id)).orElseThrow();
    }

    public List<Match> getByClubId(String clubId) {
        return matchRepo.findUpcomingMatchesByClub(UUID.fromString(clubId));
    }

    @Override
    public Match save(Match e) {
        
        return matchRepo.save(e);
    }

    @Override
    public Match delete(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    public List<Match> autoGenFixturesMatches(LocalDate startDate, List<LocalTime> matchSlots) {
        List<Club> clubs = clubRepo.findAll().stream()
                .filter(clb -> clb.getClubLogoPath() != null && clb.getClubLogoPath().contains("images") && clb.getIsActive())
                .collect(Collectors.toList());

        List<Match> matches = new ArrayList<>();
        int numTeams = clubs.size();

        if (numTeams < 2)
            return matches;

        int numRounds = numTeams - 1;
        int halfSize = numTeams / 2;

        List<Club> rotation = new ArrayList<>(clubs);
        Club fixed = rotation.remove(0); // Giữ cố định

        for (int round = 0; round < numRounds; round++) {
            String roundDescription = "Vòng " + (round + 1);
            List<Match> existingMatches = matchRepo.findByMatchDescription(roundDescription).stream()
                    .filter(m -> m.getMatchStartTime().getYear() == LocalDate.now().getYear()).toList();
            List<Match> roundMatches = new ArrayList<>();
            Set<Club> usedClubs = new HashSet<>();
            if (!existingMatches.isEmpty()) {
                matches.addAll(existingMatches); // đã có → lấy từ DB
                existingMatches.forEach(m -> {
                    usedClubs.add(m.getMatchClubStats().get(0).getClub());
                    usedClubs.add(m.getMatchClubStats().get(1).getClub());
                });
                if (existingMatches.size() == halfSize) {
                    continue;
                }
            }

            Collections.shuffle(rotation); // xáo vị trí mỗi vòng

            for (int i = 0; i < halfSize; i++) {
                Club home = (i == 0) ? fixed : rotation.get(i - 1);
                Club away = rotation.get(rotation.size() - i - 1);

                if (usedClubs.contains(home) || usedClubs.contains(away))
                    continue;

                Match match = new Match();
                match.setMatchId(UUID.randomUUID());
                match.setMatchTitle(home.getClubName() + " vs " + away.getClubName());
                match.setMatchDescription("Vòng " + (round + 1));

                MatchClubStat homeStat = new MatchClubStat(null, 0, 0, 0, 0, 0, 0, 0, match, home);
                MatchClubStat awayStat = new MatchClubStat(null, 0, 0, 0, 0, 0, 0, 0, match, away);
                match.setMatchClubStats(List.of(homeStat, awayStat));

                roundMatches.add(match);
                usedClubs.add(home);
                usedClubs.add(away);
            }

            LocalDate weekStart = startDate.plusWeeks(round);

            // Chia lịch thành 2 ngày
            LocalDate day1 = weekStart.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
            LocalDate day2 = day1.plusDays(1); // Chủ nhật

            int totalMatches = roundMatches.size();
            int split = (int) Math.ceil(totalMatches / 2.0); // 4-3

            for (int i = 0; i < totalMatches; i++) {
                Match match = roundMatches.get(i);

                LocalDate matchDate = (i < split) ? day1 : day2;
                int slotIndex = i % matchSlots.size();
                LocalTime matchTime = matchSlots.get(slotIndex);

                match.setMatchStartTime(matchDate.atTime(matchTime));
                match.setMatchLinkLivestream("https://example.com/live/" + match.getMatchId());

                matches.add(match);
            }
        }

        return matches;
    }

    public List<Match> getByRound(Integer roudNumber) {
        return getAll().stream().filter((Match m) -> m.getMatchDescription().split(" ")[1].equals(roudNumber + ""))
                .toList();
    }

    public List<Match> saveAuto(List<Match> list) {
        List<Match> newMatches = matchRepo.saveAll(list);

        AtomicInteger index = new AtomicInteger(0);
        for (Match match : list) {
            Match savedMatch = newMatches.get(index.getAndIncrement());

            match.getMatchClubStats().forEach(mcs -> {
                mcs.setMatch(savedMatch);

            });
            matchClubStatRepo.saveAll(match.getMatchClubStats());
            
        }
        return newMatches;

    }

    @Transactional(readOnly = true)
    public List<Player> getStartingLineup(String matchId, String clubId) {
        Match match = getById(matchId);
        Club club = clubRepo.findById(UUID.fromString(clubId)).orElseThrow();
        List<Player> startingLineup = match.getMatchPlayerStats().stream()
                .filter(stat -> stat.getPlayer().getClub().equals(club) && Boolean.TRUE.equals(stat.getIsStarter()))
                .map(MatchPlayerStat::getPlayer)
                .collect(Collectors.toList());

        return startingLineup;
    }

    @Transactional(readOnly = true)
    public List<Player> getSubstitueLineup(String matchId, String clubId) {
        Match match = getById(matchId);
        Club club = clubRepo.findById(UUID.fromString(clubId)).orElseThrow();
        List<Player> substituteLineup = match.getMatchPlayerStats().stream()
                .filter(stat -> stat.getPlayer().getClub().equals(club) && Boolean.FALSE.equals(stat.getIsStarter()))
                .map(MatchPlayerStat::getPlayer)
                .collect(Collectors.toList());

        return substituteLineup;
    }

    @Transactional(readOnly = true)
    public List<Match> getMatchesBetween(LocalDateTime aroundTime) {
        LocalDateTime start = aroundTime.minusMinutes(15);
        LocalDateTime end = aroundTime.plusMinutes(15);
        return matchRepo.findMatchesBetween(start, end);
    }

}
