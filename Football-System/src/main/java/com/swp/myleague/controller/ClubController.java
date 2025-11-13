package com.swp.myleague.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.myleague.model.entities.ClubSubscriber;
import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.entities.information.Club;
import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.model.entities.information.PlayerPosition;
import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.entities.match.MatchClubStat;
import com.swp.myleague.model.repo.ClubSubscriberRepo;
import com.swp.myleague.model.service.UserService;
import com.swp.myleague.model.service.blogservice.BlogService;
import com.swp.myleague.model.service.informationservice.ClubService;
import com.swp.myleague.model.service.informationservice.PlayerService;
import com.swp.myleague.model.service.matchservice.MatchClubStatService;
import com.swp.myleague.model.service.matchservice.MatchEventService;
import com.swp.myleague.model.service.matchservice.MatchService;
import com.swp.myleague.utils.GoogleMapApiService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping(value = { "/club", "/club/" })
public class ClubController {

    @Autowired
    ClubService clubService;

    @Autowired
    PlayerService playerService;

    @Autowired
    MatchService matchService;

    @Autowired
    MatchClubStatService matchClubStatService;

    @Autowired
    GoogleMapApiService googleMapApiService;

    @Autowired
    BlogService blogService;

    @Autowired
    MatchEventService matchEventService;

    @Autowired
    ClubSubscriberRepo clubSubscriberRepo;

    @Autowired
    UserService userService;

    @GetMapping("")
    public String getAllClubs(Model model) {
        model.addAttribute("clubs",
                clubService.getAll().stream().filter(c -> c.getClubLogoPath().contains("/images/logoclub")).toList());
        model.addAttribute("allTimeClubs", clubService.getAll());
        return "Club";
    }

    @GetMapping("/{clubId}")
public String getDetailClub(
        @RequestParam(name = "season", required = false) String season,
        @PathVariable("clubId") String clubId,
        Model model,
        @RequestParam(name = "subscribed", required = false) Boolean subscribed,
        Principal principal) {

    // 🔒 Nếu chưa đăng nhập thì chuyển về trang login
    if (principal == null) {
        return "redirect:/auth/login";
    }

    Club club = clubService.getById(clubId);
    model.addAttribute("club", club);
    model.addAttribute("blogs", blogService.getByClubId(clubId));
    model.addAttribute("topScorer", playerService.getTopScorerOfClub(clubId));
    model.addAttribute("topAppeared", playerService.getTopAppearedOfClub(clubId));
    model.addAttribute("highlights", matchEventService.getAllHighlightByClubId(club.getClubId().toString()));

    if (season == null || season.isBlank()) {
        season = LocalDate.now().getYear() + "";
    }

    model.addAttribute("stats", clubService.getClubStatOverview(clubId.toString(), season));
    List<Integer> seasons = new ArrayList<>();
    seasons.add(2025);
    seasons.add(2024);
    model.addAttribute("seasons", seasons);
    model.addAttribute("selectedSeason", season);

    // 🔹 Kiểm tra đăng ký theo dõi CLB
    String username = principal.getName();
    boolean isSubscribed = false;

    if (username != null) {
        User user = userService.findByUsername(username);
        if (user != null) {
            Optional<ClubSubscriber> csOpt = clubSubscriberRepo.findByEmail(user.getEmail());
            if (csOpt.isPresent()) {
                isSubscribed = true;
            }
        }
    }

    model.addAttribute("subscribed", isSubscribed);

    return "DetailClubOverview";
}


    @GetMapping("/{clubId}/squad")
    public String getSquadClub(@PathVariable("clubId") String clubId, Model model) {
    model.addAttribute("club", clubService.getById(clubId));

    List<Player> allPlayers = playerService.getPlayersByClubId(clubId);

    // 🧤 Goalkeepers
    model.addAttribute("goalkeepers", allPlayers.stream()
            .filter(p -> p.getPlayerPosition() == PlayerPosition.GK)
            .toList());

    // 🛡️ Defenders
    model.addAttribute("defenders", allPlayers.stream()
            .filter(p -> {
                PlayerPosition pos = p.getPlayerPosition();
                return pos == PlayerPosition.CB
                        || pos == PlayerPosition.RB
                        || pos == PlayerPosition.LB
                        || pos == PlayerPosition.RWB
                        || pos == PlayerPosition.LWB;
            })
            .toList());

    // ⚙️ Midfielders (gộp MD, CM, CDM, AM)
    model.addAttribute("midfielders", allPlayers.stream()
            .filter(p -> {
                PlayerPosition pos = p.getPlayerPosition();
                return pos == PlayerPosition.MD
                        || pos == PlayerPosition.CM
                        || pos == PlayerPosition.CDM
                        || pos == PlayerPosition.AM;
            })
            .toList());

    // ⚔️ Forwards
    model.addAttribute("forwards", allPlayers.stream()
            .filter(p -> {
                PlayerPosition pos = p.getPlayerPosition();
                return pos == PlayerPosition.FW
                        || pos == PlayerPosition.SS
                        || pos == PlayerPosition.ST
                        || pos == PlayerPosition.LFW
                        || pos == PlayerPosition.RFW;
            })
            .toList());

    return "DetailClubSquad";
}

    @GetMapping("/player/{playerId}")
    public String getDetailPlayer(@PathVariable("playerId") String playerId, Model model) {
        model.addAttribute("player", playerService.getById(playerId));
        model.addAttribute("career", playerService.getCareerByPlayerId(playerId));
        return "DetailPlayer";
    }

    @GetMapping("/{clubId}/results")
    public String getClubResults(@PathVariable("clubId") String clubId, Model model) {
        model.addAttribute("club", clubService.getById(clubId));
        try {
            List<MatchClubStat> matchClubStats = matchClubStatService.getAllByClubId(clubId);
            List<Match> matches = matchService.getAll().stream()
                    .filter(m -> m.getMatchClubStats().stream()
                            .anyMatch(mcs -> matchClubStats.stream()
                                    .anyMatch(mcss -> mcss.getMatchClubStatId().equals(mcs.getMatchClubStatId()))))
                    .toList();
            model.addAttribute("matches", matches);
            return "DetailClubResults";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "DetailClubOverview";

    }

    @GetMapping("/{clubId}/fixtures")
    public String getStatsOfClub(@PathVariable("clubId") String clubId, Model model) {
        model.addAttribute("club", clubService.getById(clubId));
        model.addAttribute("matches", matchService.getByClubId(clubId));
        return "DetailClubFixture";
    }

    @GetMapping("/{clubId}/stadium")
    public String showMap(@PathVariable("clubId") String clubId, Model model) {
        model.addAttribute("club", clubService.getById(clubId));
        return "DetailClubStadium";
    }

}