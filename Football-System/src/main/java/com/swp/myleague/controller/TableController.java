package com.swp.myleague.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.myleague.model.service.informationservice.ClubService;
import com.swp.myleague.model.service.matchservice.MatchClubStatService;
import com.swp.myleague.payload.ClubStandingDTO;

import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequestMapping(value = {"/tables", "/tables/"})
public class TableController {
    
    @Autowired
    ClubService clubService;

    @Autowired
    MatchClubStatService matchClubStatService;

    @GetMapping("")
    public String getTableClubs(@RequestParam(name = "season", required = false, defaultValue = "2025") String seasonStr ,Model model) {
        Integer season = Integer.parseInt(seasonStr);
        List<ClubStandingDTO> clubStandingDTOs = matchClubStatService.getClubStandings(season);

        List<String> seasons = new ArrayList<>();
        seasons.add("2024");
        seasons.add("2025");

        model.addAttribute("teams", clubStandingDTOs);
        model.addAttribute("seasons", seasons);
        model.addAttribute("selectedSeason", seasonStr); 
        return "Table";
    }
    

}
