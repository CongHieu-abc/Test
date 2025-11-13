package com.swp.myleague.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.swp.myleague.model.entities.blog.Blog;
import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.entities.match.MatchEvent;
import com.swp.myleague.model.entities.match.MatchEventType;
import com.swp.myleague.model.service.blogservice.BlogService;
import com.swp.myleague.model.service.informationservice.ClubService;
import com.swp.myleague.model.service.matchservice.MatchClubStatService;
import com.swp.myleague.model.service.matchservice.MatchEventService;
import com.swp.myleague.model.service.matchservice.MatchService;
import com.swp.myleague.payload.ClubStandingDTO;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping(value = { "/home", "/home/" })
public class HomeController {

    @Autowired
    ClubService clubService;

    @Autowired
    MatchClubStatService matchClubStatService;

    @Autowired
    MatchService matchService;

    @Autowired
    BlogService blogService;

    @Autowired
    MatchEventService matchEventService;

    @GetMapping("")
    public String returnHome(Model model) {
        Integer season = LocalDate.now().getYear();
        List<ClubStandingDTO> clubStandingDTOs = matchClubStatService.getClubStandings(season);
        model.addAttribute("teams", clubStandingDTOs);

        List<Match> matches = matchService.getAll().stream()
                .filter(m -> m.getMatchStartTime().compareTo(LocalDateTime.now()) > 0).toList();
        Map<LocalDate, List<Match>> groupedByDate = matches.stream()
                .collect(Collectors.groupingBy(match -> match.getMatchStartTime().toLocalDate()));

        model.addAttribute("matchesByDate", groupedByDate);

        List<Blog> blogs = blogService.getAll().stream()
                .sorted(Comparator.comparing(Blog::getBlogDateCreated).reversed())
                .collect(Collectors.toList());
        model.addAttribute("blogs", blogs);

        List<MatchEvent> highlights = matchEventService.getAll().stream()
                .filter(e -> e.getMatchEventType() == MatchEventType.Highlight)
                .sorted(Comparator.comparing(MatchEvent::getDateCreated))
                .collect(Collectors.toList());
        model.addAttribute("hightlights", highlights);

        return "Home";
    }

}
