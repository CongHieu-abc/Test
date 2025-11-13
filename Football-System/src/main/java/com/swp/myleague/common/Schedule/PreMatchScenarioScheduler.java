package com.swp.myleague.common.Schedule;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.entities.match.MatchClubStat;
import com.swp.myleague.model.entities.match.MatchEvent;
import com.swp.myleague.model.entities.match.MatchEventType;
import com.swp.myleague.model.entities.match.MatchPlayerStat;
import com.swp.myleague.model.repo.MatchClubStatRepo;
import com.swp.myleague.model.repo.MatchEventRepo;
import com.swp.myleague.model.repo.MatchPlayerStatRepo;
import com.swp.myleague.model.repo.MatchRepo;
import com.swp.myleague.model.repo.PlayerRepo;
import com.swp.myleague.model.service.matchservice.MatchService;
import com.swp.myleague.utils.gemini_matchevent.GeminiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PreMatchScenarioScheduler {

    private final MatchRepo matchRepo;

    private final MatchService matchService;

    // private final LlamaClientService llamaClientService;

    // private final OpenAiService openAiService;

    private final GeminiClient geminiClient;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Ho_Chi_Minh")
    public void generateScenarioForUpcomingMatches() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime threshold = now.plusMinutes(90);

        List<Match> upcoming = matchRepo.findUpcomingMatches(now, threshold);

        for (Match match : upcoming) {
            try {
                // Kiểm tra xem cả 2 đội đều đã có starting lineup chưa
                if (match.getMatchClubStats() == null || match.getMatchClubStats().size() < 2) {
                    log.warn("Match {} không có đủ 2 đội, bỏ qua việc tạo scenario.", match.getMatchTitle());
                    continue;
                }
                
                // Lấy starting lineup của cả 2 đội
                String matchId = match.getMatchId().toString();
                String club1Id = match.getMatchClubStats().get(0).getClub().getClubId().toString();
                String club2Id = match.getMatchClubStats().get(1).getClub().getClubId().toString();
                
                List<Player> team1Starters = matchService.getStartingLineup(matchId, club1Id);
                List<Player> team2Starters = matchService.getStartingLineup(matchId, club2Id);
                
                boolean team1HasLineup = team1Starters != null && !team1Starters.isEmpty();
                boolean team2HasLineup = team2Starters != null && !team2Starters.isEmpty();
                
                if (!team1HasLineup || !team2HasLineup) {
                    String missingTeam = !team1HasLineup ? match.getMatchClubStats().get(0).getClub().getClubName() : 
                                        match.getMatchClubStats().get(1).getClub().getClubName();
                    log.warn("Match {} chưa có đủ lineup cho cả 2 đội. Đội thiếu lineup: {}. " +
                            "Vui lòng import đủ lineup cho cả 2 đội trước khi trận đấu diễn ra.", 
                            match.getMatchTitle(), missingTeam);
                    continue;
                }
                
                // Kiểm tra xem match đã có events chưa (tránh tạo trùng)
                if (match.getMatchEvents() != null && !match.getMatchEvents().isEmpty()) {
                    log.info("Match {} đã có events ({} events), bỏ qua việc tạo scenario mới.", 
                            match.getMatchTitle(), match.getMatchEvents().size());
                    continue;
                }
                
                String prompt = buildPrompt(match);
                String script = generateWithRetry(prompt, match.getMatchTitle());

                // Kiểm tra nếu script là null hoặc thông báo lỗi
                if (script == null || script.trim().isEmpty()) {
                    log.error("Gemini trả về script null hoặc rỗng cho match {} sau khi retry", match.getMatchTitle());
                    continue;
                }
                
                if (script.contains("❌") || script.contains("⚠️") || script.contains("Lỗi")) {
                    log.error("Gemini trả về lỗi sau khi retry, bỏ qua match {}: {}", match.getMatchTitle(), script);
                    continue;
                }

                // Log script content để debug (không chỉ debug level)
                log.info("Script từ Gemini ({} chars):\n{}", script.length(), script);

                applyMatchScript(match, script);

                log.info("Generated scenario for match {}", match.getMatchTitle());
            } catch (Exception ex) {
                log.error("Failed to create scenario for match {}", match.getMatchId(), ex);
            }
        }
    }

    @Autowired
    private MatchRepo matchRepository;

    @Autowired
    private PlayerRepo playerRepository;

    @Autowired
    private MatchEventRepo matchEventRepository;

    @Autowired
    private MatchClubStatRepo matchClubStatRepository;

    @Autowired
    private MatchPlayerStatRepo matchPlayerStatRepository;

    public void applyMatchScript(Match match, String script) {
        List<MatchEvent> events = new ArrayList<>();

        // Log script để debug
        log.info("=== Xử lý script cho match {} ===", match.getMatchTitle());
        log.info("Script length: {} chars", script.length());

        // Làm sạch script: loại bỏ markdown code blocks
        script = cleanScript(script);

        List<MatchPlayerStat> playerStats = matchPlayerStatRepository.findByMatch(match);
        
        // Kiểm tra lại lineup trước khi xử lý
        if (playerStats == null || playerStats.isEmpty()) {
            log.warn("Match {} không có playerStats, không thể tạo events. Bỏ qua.", match.getMatchTitle());
            return;
        }
        
        boolean hasStarter = playerStats.stream()
                .anyMatch(stat -> stat.getIsStarter() != null && stat.getIsStarter());
        
        if (!hasStarter) {
            log.warn("Match {} không có starting lineup, không thể tạo events. Bỏ qua.", match.getMatchTitle());
            return;
        }
        
        Map<UUID, MatchPlayerStat> playerStatsMap = playerStats.stream()
                .collect(Collectors.toMap(stat -> stat.getPlayer().getPlayerId(), stat -> stat));

        // Tạo map để tìm player theo tên (để xử lý khi Gemini trả về tên thật)
        Map<String, Player> playerNameMap = new HashMap<>();
        for (MatchPlayerStat stat : playerStats) {
            Player player = stat.getPlayer();
            playerNameMap.put(player.getPlayerFullName().toLowerCase(), player);
            // Cũng thêm format "Player X-Y" nếu có
            if (player.getPlayerFullName().matches("Player\\s+\\d+-\\d+")) {
                playerNameMap.put(player.getPlayerFullName(), player);
            }
        }

        Map<UUID, Integer> playerAppearMinutes = new HashMap<>();

        String[] lines = script.split("\\R");
        log.info("Tổng số dòng trong script: {}", lines.length);

        int processedLines = 0;
        int skippedLines = 0;
        
        for (String line : lines) {
            try {
                line = line.trim();
                
                // Bỏ qua các dòng markdown hoặc không hợp lệ
                if (line.isEmpty() || 
                    line.startsWith("```") || 
                    line.startsWith("#") ||
                    !line.contains(":")) {
                    skippedLines++;
                    continue;
                }

                String[] parts = line.split(":", 3);
                if (parts.length < 2) {
                    skippedLines++;
                    log.debug("Skipped line (không đủ parts): {}", line);
                    continue;
                }

                Integer minute = parseMinute(parts[0].trim());
                // Nếu có 3 parts, lấy parts[2], nếu chỉ có 2 parts, lấy parts[1]
                String content = (parts.length >= 3) ? parts[2].trim() : parts[1].trim();
                
                if (minute == null || minute < 0 || minute > 120) {
                    skippedLines++;
                    log.debug("Skipped line (minute không hợp lệ: {}): {}", minute, line);
                    continue;
                }
                
                processedLines++;

                MatchEvent event = new MatchEvent();
                event.setMatch(match);
                event.setMatchEventMinute(minute);
                event.setMatchEventTitle(content);
                event.setMatchEventType(determineEventType(content));

                // Tìm Player ID nếu có trong câu (hỗ trợ cả "Player X-Y" và tên thật)
                Optional<Player> playerOpt = extractPlayerByName(content, playerNameMap);
                playerOpt.ifPresent(player -> {
                    int prevMinute = playerAppearMinutes.getOrDefault(player.getPlayerId(), 0);
                    playerAppearMinutes.put(player.getPlayerId(), Math.max(prevMinute, minute));
                    event.setPlayer(player);

                    MatchPlayerStat stat = playerStatsMap.get(player.getPlayerId());
                    if (stat != null) {
                        if (event.getMatchEventType() == MatchEventType.Goal) {
                            stat.setMatchPlayerGoal(
                                    (stat.getMatchPlayerGoal() == null ? 0 : stat.getMatchPlayerGoal()) + 1);
                            
                            // Cập nhật tỷ số cho câu lạc bộ của cầu thủ ghi bàn
                            for (MatchClubStat clubStat : match.getMatchClubStats()) {
                                if (clubStat.getClub().getClubId().equals(player.getClub().getClubId())) {
                                    clubStat.setMatchClubStatScore(
                                            (clubStat.getMatchClubStatScore() == null ? 0 : clubStat.getMatchClubStatScore()) + 1);
                                    matchClubStatRepository.save(clubStat);
                                    break;
                                }
                            }
                        } else if (event.getMatchEventType() == MatchEventType.Shoot) {
                            stat.setMatchPlayerShoots(
                                    (stat.getMatchPlayerShoots() == null ? 0 : stat.getMatchPlayerShoots()) + 1);
                        }
                        matchPlayerStatRepository.save(stat);
                    }
                });

                // Cập nhật cho club nếu thấy tên Club
                // Xử lý Goal không có player (ví dụ: phản lưới nhà) hoặc các sự kiện khác
                // Chỉ xử lý Goal ở đây nếu event chưa có player (tránh cập nhật 2 lần)
                if (event.getMatchEventType() == MatchEventType.Goal && event.getPlayer() == null) {
                    // Trường hợp Goal nhưng không có player (ví dụ: phản lưới nhà)
                    // Cập nhật tỷ số cho câu lạc bộ được đề cập trong content
                    for (MatchClubStat clubStat : match.getMatchClubStats()) {
                        if (content.contains(clubStat.getClub().getClubName())) {
                            clubStat.setMatchClubStatScore(
                                    (clubStat.getMatchClubStatScore() == null ? 0 : clubStat.getMatchClubStatScore()) + 1);
                            matchClubStatRepository.save(clubStat);
                            break;
                        }
                    }
                } else {
                    // Các sự kiện khác (YellowCard, Shoot) hoặc Goal đã được xử lý với player
                    for (MatchClubStat clubStat : match.getMatchClubStats()) {
                        if (content.contains(clubStat.getClub().getClubName())) {
                            if (event.getMatchEventType() == MatchEventType.YellowCard) {
                                clubStat.setMatchClubStatYellowCard((clubStat.getMatchClubStatYellowCard() == null ? 0
                                        : clubStat.getMatchClubStatYellowCard()) + 1);
                                matchClubStatRepository.save(clubStat);
                            } else if (event.getMatchEventType() == MatchEventType.Shoot) {
                                clubStat.setMatchClubStatShoots(
                                        (clubStat.getMatchClubStatShoots() == null ? 0 : clubStat.getMatchClubStatShoots())
                                                + 1);
                                matchClubStatRepository.save(clubStat);
                            }
                        }
                    }
                }

                // Chỉ add vào list, sẽ lưu tất cả cùng lúc sau
                events.add(event);
                match.getMatchPlayerStats().forEach(mps -> mps.setMatchPlayerMinutedPlayed(minute));
            } catch (Exception e) {
                skippedLines++;
                log.error("❌ Lỗi khi xử lý dòng: {}", line, e);
            }
        }
        
        log.info("Đã xử lý: {} dòng, Bỏ qua: {} dòng, Tạo được: {} events", 
                processedLines, skippedLines, events.size());

        // Lưu tất cả events vào database cùng lúc
        if (!events.isEmpty()) {
            try {
                List<MatchEvent> savedEvents = matchEventRepository.saveAll(events);
                log.info("Đã lưu {} events vào database cho match {}", savedEvents.size(), match.getMatchTitle());
                
                // Lưu lại matchPlayerStats (đã được cập nhật trong vòng lặp)
                if (match.getMatchPlayerStats() != null && !match.getMatchPlayerStats().isEmpty()) {
                    matchPlayerStatRepository.saveAll(match.getMatchPlayerStats());
                }
                
                // Reload match từ database để đảm bảo events được load đúng
                Match reloadedMatch = matchRepository.findById(match.getMatchId()).orElse(match);
                log.info("Match {} đã được reload từ database với {} events", 
                        reloadedMatch.getMatchTitle(), 
                        reloadedMatch.getMatchEvents() != null ? reloadedMatch.getMatchEvents().size() : 0);
            } catch (Exception e) {
                log.error("❌ Lỗi khi lưu events vào database cho match {}: {}", match.getMatchTitle(), e.getMessage(), e);
                throw e;
            }
        } else {
            log.warn("Không có events nào để lưu cho match {}", match.getMatchTitle());
        }
    }
    
    /**
     * Gọi Gemini API với retry logic khi gặp lỗi 503 (Service Unavailable)
     */
    private String generateWithRetry(String prompt, String matchTitle) {
        int maxRetries = 3;
        long baseDelayMs = 2000; // 2 giây
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String script = geminiClient.generate(prompt);
                
                // Kiểm tra nếu là lỗi 503 (Service Unavailable)
                if (script != null && script.contains("503") && script.contains("Service Unavailable")) {
                    if (attempt < maxRetries) {
                        long delayMs = baseDelayMs * (long) Math.pow(2, attempt - 1); // Exponential backoff
                        log.warn("Gemini API trả về 503 (attempt {}/{}), đợi {}ms trước khi retry cho match {}", 
                                attempt, maxRetries, delayMs, matchTitle);
                        try {
                            Thread.sleep(delayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error("Thread bị interrupt khi đợi retry");
                            return script; // Trả về lỗi hiện tại
                        }
                        continue; // Retry
                    } else {
                        log.error("Gemini API vẫn trả về 503 sau {} lần thử cho match {}", maxRetries, matchTitle);
                        return script; // Trả về lỗi cuối cùng
                    }
                }
                
                // Nếu không phải lỗi 503 hoặc đã retry hết, trả về kết quả
                return script;
                
            } catch (Exception e) {
                if (attempt < maxRetries) {
                    long delayMs = baseDelayMs * (long) Math.pow(2, attempt - 1);
                    log.warn("Lỗi khi gọi Gemini API (attempt {}/{}), đợi {}ms trước khi retry cho match {}: {}", 
                            attempt, maxRetries, delayMs, matchTitle, e.getMessage());
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Thread bị interrupt khi đợi retry");
                        return "❌ Lỗi Gemini: " + e.getMessage();
                    }
                } else {
                    log.error("Lỗi khi gọi Gemini API sau {} lần thử cho match {}: {}", 
                            maxRetries, matchTitle, e.getMessage(), e);
                    return "❌ Lỗi Gemini: " + e.getMessage() + " (Class: " + e.getClass().getSimpleName() + ")";
                }
            }
        }
        
        // Không bao giờ đến đây, nhưng để đảm bảo compile
        return "❌ Lỗi Gemini: Không thể tạo script sau " + maxRetries + " lần thử";
    }
    
    /**
     * Làm sạch script từ Gemini: loại bỏ markdown code blocks và formatting
     */
    private String cleanScript(String script) {
        if (script == null || script.isEmpty()) {
            return script;
        }
        
        // Loại bỏ markdown code blocks (cả ```code``` và ``` ở đầu/cuối)
        script = script.replaceAll("```[\\w]*", "");
        script = script.replaceAll("```", "");
        
        // Loại bỏ các dòng markdown header và formatting
        String[] lines = script.split("\\R");
        StringBuilder cleaned = new StringBuilder();
        for (String line : lines) {
            line = line.trim();
            // Bỏ qua dòng header markdown
            if (line.startsWith("#")) {
                continue;
            }
            // Loại bỏ bullet points ở đầu dòng
            line = line.replaceFirst("^[\\*\\-\\+\\>]\\s+", "");
            // Loại bỏ số thứ tự (1. 2. etc)
            line = line.replaceFirst("^\\d+\\.\\s+", "");
            if (!line.isEmpty()) {
                cleaned.append(line).append("\n");
            }
        }
        
        return cleaned.toString().trim();
    }

    private Integer parseMinute(String timestamp) {
        try {
            // Đơn giản hóa: timestamp chỉ là số phút (0-90)
            // Ví dụ: "0", "15", "45", "90"
            int minute = Integer.parseInt(timestamp.trim());
            if (minute >= 0 && minute <= 120) {
                return minute;
            }
            log.warn("Minute ngoài phạm vi hợp lệ (0-120): {}", minute);
            return null;
        } catch (NumberFormatException e) {
            // Nếu không parse được số, thử parse format "HH:MM" hoặc "MM:SS"
            try {
                String[] parts = timestamp.trim().split(":");
                if (parts.length == 2) {
                    int first = Integer.parseInt(parts[0]);
                    int second = Integer.parseInt(parts[1]);
                    
                    // Nếu first > 90, chắc chắn là HH:MM format
                    if (first > 90) {
                        return null; // Không hợp lệ cho bóng đá
                    }
                    
                    // Nếu first <= 90 và second < 60, có thể là:
                    // - MM:SS (ví dụ: "45:30" = phút 45)
                    // - HH:MM nếu first <= 2 (ví dụ: "01:30" = 90 phút, "01:00" = 60 phút, "02:00" = 120 phút)
                    if (first <= 2 && second >= 0 && second < 60) {
                        // Có thể là HH:MM, tính thành phút
                        return first * 60 + second;
                    } else if (first > 2 && second >= 0 && second < 60) {
                        // Chắc chắn là MM:SS, chỉ lấy MM
                        return first;
                    }
                }
            } catch (Exception ex) {
                // Ignore
            }
            log.warn("Không thể parse minute từ: {}", timestamp);
            return null;
        }
    }

    /**
     * Tìm player từ content, hỗ trợ cả format "Player X-Y" và tên thật
     */
    private Optional<Player> extractPlayerByName(String content, Map<String, Player> playerNameMap) {
        // Thử tìm theo format "Player X-Y"
        Pattern pattern = Pattern.compile("Player\\s+\\d+-\\d+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String playerName = matcher.group();
            Optional<Player> player = playerRepository.findByPlayerFullName(playerName);
            if (player.isPresent()) {
                return player;
            }
        }
        
        // Thử tìm theo tên thật từ playerNameMap
        content = content.toLowerCase();
        for (Map.Entry<String, Player> entry : playerNameMap.entrySet()) {
            String playerNameLower = entry.getKey().toLowerCase();
            // Tìm tên player trong content (tên phải có độ dài hợp lý để tránh match sai)
            if (playerNameLower.length() > 3 && content.contains(playerNameLower)) {
                return Optional.of(entry.getValue());
            }
        }
        
        return Optional.empty();
    }

    private MatchEventType determineEventType(String content) {
        String contentLower = content.toLowerCase();
        
        // Yellow Card - Tiếng Việt và Tiếng Anh
        if (contentLower.contains("thẻ vàng") || 
            contentLower.contains("yellow card") ||
            contentLower.contains("caution") ||
            contentLower.contains("booking"))
            return MatchEventType.YellowCard;
        
        // Red Card - Tiếng Việt và Tiếng Anh
        if (contentLower.contains("thẻ đỏ") || 
            contentLower.contains("truất quyền thi đấu") ||
            contentLower.contains("red card") ||
            contentLower.contains("sent off") ||
            contentLower.contains("dismissal"))
            return MatchEventType.RedCard;
        
        // Goal - Tiếng Việt và Tiếng Anh (kiểm tra trước Shoot để ưu tiên Goal khi có cả 2)
        // Các từ khóa rõ ràng về ghi bàn
        if (contentLower.contains("ghi bàn") || 
            contentLower.contains("lập công") || 
            contentLower.contains("mở tỷ số") ||
            contentLower.contains("nâng tỷ số") || 
            contentLower.contains("gỡ hòa") || 
            contentLower.contains("ấn định") ||
            contentLower.contains("rút ngắn") ||
            contentLower.contains("phản lưới") ||
            contentLower.contains("scored") ||
            contentLower.contains("scores") ||
            contentLower.contains("into the net") ||
            contentLower.contains("finds the net") ||
            contentLower.contains("hits the net") ||
            contentLower.contains("equalizer") ||
            contentLower.contains("equaliser") ||
            contentLower.contains("opener") ||
            contentLower.contains("opening goal") ||
            (contentLower.contains("goal") && 
             !contentLower.contains("towards goal") &&
             !contentLower.contains("shot on goal") &&
             !contentLower.contains("shoot on goal")))
            return MatchEventType.Goal;
        
        // Shoot - Tiếng Việt và Tiếng Anh
        // Các từ khóa này chỉ ra hành động sút, không nhất thiết là ghi bàn
        if (contentLower.contains("dứt điểm") || 
            contentLower.contains("cú sút") || 
            contentLower.contains("sút bóng") ||
            contentLower.contains("vô lê") ||
            contentLower.contains("đánh đầu") || 
            contentLower.contains("cú đá") ||
            contentLower.contains("shot") ||
            contentLower.contains("shoot") ||
            contentLower.contains("shooting") ||
            contentLower.contains("attempt") ||
            contentLower.contains("header") ||
            contentLower.contains("strike") ||
            contentLower.contains("towards goal") ||
            contentLower.contains("shot on goal") ||
            contentLower.contains("shoot on goal"))
            return MatchEventType.Shoot;
        
        // Mặc định là Highlight
        return MatchEventType.Highlight;
    }

    private record TeamLineup(
            String clubName,
            List<Player> starters,
            List<Player> substitutes) {
    }

    private TeamLineup extractLineup(Match match, MatchClubStat stat) {
        String matchId = match.getMatchId().toString();
        String clubId = stat.getClub().getClubId().toString();
        String clubName = stat.getClub().getClubName();

        List<Player> starters = matchService.getStartingLineup(matchId, clubId);
        List<Player> substitutes = matchService.getSubstitueLineup(matchId, clubId);

        return new TeamLineup(clubName, starters, substitutes);
    }

    /** Tạo prompt hướng dẫn ChatGPT */
    private String buildPrompt(Match m) {
        TeamLineup team1 = extractLineup(m, m.getMatchClubStats().get(0));
        TeamLineup team2 = extractLineup(m, m.getMatchClubStats().get(1));
        
        // Kiểm tra xem cả 2 đội đều có starting lineup hợp lệ
        boolean team1HasLineup = team1.starters() != null && !team1.starters().isEmpty();
        boolean team2HasLineup = team2.starters() != null && !team2.starters().isEmpty();
        
        if (!team1HasLineup || !team2HasLineup) {
            String missingTeam = !team1HasLineup ? team1.clubName() : team2.clubName();
            log.warn("Match {} không có starting lineup cho cả 2 đội. Đội thiếu lineup: {}. Không thể tạo prompt.", 
                    m.getMatchTitle(), missingTeam);
            throw new IllegalStateException("Match không có đủ starting lineup cho cả 2 đội. Đội thiếu: " + missingTeam);
        }

        String prompt = """
                Bạn là bình luận viên bóng đá. Hãy tạo kịch bản diễn biến 90 phút trận đấu ở mức KHỞI ĐẦU
                (những sự kiện quan trọng dự kiến, không quá chi tiết) cho trận:

                - Tiêu đề: %s
                - Thời gian bắt đầu: %s (GMT+7)

                Danh sách cầu thủ dự kiến:

                - %s (Sân nhà)
                Đội hình chính:
                %s

                Dự bị:
                %s

                - %s (Sân khách)
                Đội hình chính:
                %s

                Dự bị:
                %s

                Yêu cầu:
                1. Trả về **CHỈ TEXT THUẦN**, KHÔNG có markdown, KHÔNG có code blocks. Mỗi dòng dạng "MM:Nội dung" (MM là số phút từ 0-90).
                   Ví dụ: `0:Trọng tài thổi còi khai cuộc`
                   Ví dụ: `15:Brentford tấn công mạnh mẽ`
                   Ví dụ: `45:Player 1-10 ghi bàn mở tỷ số cho Brentford`

                2. Định dạng chuẩn cho mỗi dòng: "MM:Nội dung sự kiện"
                   - MM: Số phút trong trận đấu (0-90)
                   - Nội dung: Mô tả sự kiện bằng tiếng Việt hoặc tiếng Anh
                   - Nếu có cầu thủ, hãy ghi rõ tên cầu thủ (dùng format "Player X-Y" hoặc tên đầy đủ)
                   - Ví dụ: `23:Player 1-10 ghi bàn đẹp mắt cho Brentford`
                   - Ví dụ: `67:Yellow card cho Player 2-5 của Leeds United`

                3. Các loại sự kiện cần có (mô tả rõ ràng trong nội dung):
                   - Bàn thắng (Goal): dùng từ "ghi bàn", "goal", "scored", "mở tỷ số", "nâng tỷ số"
                   - Cú sút (Shoot): dùng từ "sút", "shot", "dứt điểm", "attempt"
                   - Thẻ vàng (YellowCard): dùng từ "thẻ vàng", "yellow card", "caution"
                   - Thẻ đỏ (RedCard): dùng từ "thẻ đỏ", "red card", "sent off"

                4. Tạo ít nhất 5-10 sự kiện quan trọng trong trận đấu, bao gồm:
                   - Sự kiện khai cuộc (phút 0)
                   - Ít nhất 2-3 bàn thắng (phút 15-85)
                   - Một số cú sút, thẻ vàng (nếu có)
                   - Sự kiện kết thúc (phút 90)

                5. PHẢI trả về đúng format "MM:Nội dung" trên mỗi dòng, không có ký tự đặc biệt, không có markdown.

                """
                .formatted(
                        m.getMatchTitle(),
                        m.getMatchStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        team1.clubName,
                        formatPlayerList(team1.starters),
                        formatPlayerList(team1.substitutes),
                        team2.clubName,
                        formatPlayerList(team2.starters),
                        formatPlayerList(team2.substitutes));

        return prompt;
    }

    private String formatPlayerList(List<Player> players) {
        return players.stream()
                .map(p -> "- ID: " + p.getPlayerId() + ", Name: " + p.getPlayerFullName()
                        + ", Position: " + p.getPlayerPosition().name()
                        + ", Number: #" + p.getPlayerNumber())
                .collect(Collectors.joining("\n"));
    }
}