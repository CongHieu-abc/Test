package com.swp.myleague.common.Schedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.entities.match.MatchClubStat;
import com.swp.myleague.model.entities.match.MatchPlayerStat;
import com.swp.myleague.model.service.EmailService;
import com.swp.myleague.model.service.UserService;
import com.swp.myleague.model.service.matchservice.MatchPlayerStatService;
import com.swp.myleague.model.service.matchservice.MatchService;

@Service
public class LineupReminderScheduler {

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchPlayerStatService matchPlayerStatService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // Gửi nhắc nhở mỗi 30 phút
    @Scheduled(cron = "0 * * * * *") // every 30 minutes
    public void checkAndSendReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Long> notifyHours = List.of(24L, 6L, 1L); // 24h, 6h, 1h trước trận

        for (Long hoursBefore : notifyHours) {
            LocalDateTime targetTime = now.plusHours(hoursBefore);
            LocalDateTime windowStart = targetTime.minusMinutes(30);
            LocalDateTime windowEnd = targetTime.plusMinutes(30);

            List<Match> matches = matchService.getAll().stream()
                    .filter(m -> {
                        LocalDateTime startTime = m.getMatchStartTime();
                        return startTime.isAfter(windowStart) && startTime.isBefore(windowEnd);
                    })
                    .toList();

            for (Match match : matches) {
                for (MatchClubStat clubStat : match.getMatchClubStats()) {
                    UUID clubId = clubStat.getClub().getClubId();

                    if (clubStat.getClub().getUserId() == null) {
                        continue;
                    }

                    User user = userService.getUserById(clubStat.getClub().getUserId().toString());

                    String clubEmail = user.getEmail();

                    List<MatchPlayerStat> playerStats = matchPlayerStatService
                            .getAllByMatchId(match.getMatchId().toString()).stream()
                            .filter(mcs -> mcs.getPlayer().getClub().getClubId().compareTo(clubId) == 0).toList();

                    if (playerStats == null || playerStats.isEmpty()) {
                        String timeLabel = hoursBefore + "h before kickoff";
                        emailService.sendLineupReminder(
                                clubEmail,
                                clubStat.getClub().getClubName(),
                                match,
                                timeLabel);
                    }
                }
            }
        }
    }
}
