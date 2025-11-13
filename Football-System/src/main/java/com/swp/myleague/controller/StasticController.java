package com.swp.myleague.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.myleague.model.repo.MatchPlayerStatRepo;
import com.swp.myleague.model.service.informationservice.ClubService;
import com.swp.myleague.model.service.informationservice.PlayerService;
import com.swp.myleague.model.service.matchservice.MatchClubStatService;
import com.swp.myleague.model.entities.information.PlayerPosition;
import com.swp.myleague.model.entities.information.Player;
import com.swp.myleague.payload.PlayerStandingCleanSheetsDTO;
import com.swp.myleague.payload.PlayerStandingPlayedMinutesDTO;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = {"/stastics", "/stastics/"})
public class StasticController {

    @Autowired MatchClubStatService matchClubStatService;

    @Autowired MatchPlayerStatRepo matchPlayerStatRepo;

    @Autowired PlayerService playerService;

    @Autowired ClubService clubService;
    
    @GetMapping("")
    public String getStastics(@RequestParam(name = "year") String year , Model model) {
        model.addAttribute("rankingPlayerGoal", playerService.getTop10PlayerGoalByYear(year));
        model.addAttribute("rankingPlayerAssist", playerService.getTop10PlayerAssistByYear(year));
        
        // Lấy dữ liệu clean sheets và minutes played
        Map<Player, PlayerStandingCleanSheetsDTO> allCleanSheets = playerService.getTop10PlayerCleanSheetByYear(year);
        Map<Player, PlayerStandingPlayedMinutesDTO> allMinutesPlayed = playerService.getTop10PlayerMinutePlayedByYear(year);
        
        // Lọc chỉ lấy GK, bỏ qua những thủ môn có 0 clean sheets, và sắp xếp
        Map<Player, PlayerStandingCleanSheetsDTO> gkCleanSheets = allCleanSheets.entrySet().stream()
                .filter(entry -> {
                    Player player = entry.getKey();
                    PlayerStandingCleanSheetsDTO dto = entry.getValue();
                    // Chỉ lấy GK và có cleanSheets > 0
                    if (player.getPlayerPosition() != PlayerPosition.GK) {
                        return false;
                    }
                    // Kiểm tra cleanSheets > 0
                    try {
                        int cleanSheets = Integer.parseInt(dto.getCleanSheets());
                        return cleanSheets > 0;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .sorted((e1, e2) -> {
                    // So sánh theo cleanSheets giảm dần
                    int cleanSheets1 = 0, cleanSheets2 = 0;
                    try {
                        cleanSheets1 = Integer.parseInt(e1.getValue().getCleanSheets());
                        cleanSheets2 = Integer.parseInt(e2.getValue().getCleanSheets());
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                    
                    if (cleanSheets1 != cleanSheets2) {
                        return Integer.compare(cleanSheets2, cleanSheets1); // Giảm dần
                    }
                    
                    // Nếu cleanSheets bằng nhau, so sánh theo minutes played
                    long minutes1 = 0, minutes2 = 0;
                    PlayerStandingPlayedMinutesDTO minutesDto1 = allMinutesPlayed.get(e1.getKey());
                    PlayerStandingPlayedMinutesDTO minutesDto2 = allMinutesPlayed.get(e2.getKey());
                    
                    if (minutesDto1 != null) {
                        try {
                            minutes1 = Long.parseLong(minutesDto1.getTotalMinutes());
                        } catch (NumberFormatException e) {
                            // Ignore
                        }
                    }
                    
                    if (minutesDto2 != null) {
                        try {
                            minutes2 = Long.parseLong(minutesDto2.getTotalMinutes());
                        } catch (NumberFormatException e) {
                            // Ignore
                        }
                    }
                    
                    return Long.compare(minutes2, minutes1); // Giảm dần
                })
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (v1, v2) -> v1,
                    LinkedHashMap::new
                ));
        
        model.addAttribute("rankingPlayerCleanSheet", gkCleanSheets);
        
        model.addAttribute("rankingPlayerMinutedPlayed", allMinutesPlayed);
        return "Stastics";
    }
    

}
