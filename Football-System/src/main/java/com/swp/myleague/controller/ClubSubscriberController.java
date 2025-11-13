package com.swp.myleague.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.myleague.model.entities.ClubSubscriber;
import com.swp.myleague.model.entities.information.Club;
import com.swp.myleague.model.repo.ClubRepo;
import com.swp.myleague.model.repo.ClubSubscriberRepo;
import com.swp.myleague.model.service.UserService;

@Controller
@RequestMapping("/subscriber")
public class ClubSubscriberController {

    @Autowired ClubRepo clubRepo;

    @Autowired ClubSubscriberRepo clubSubscriberRepo;

    @Autowired UserService userService;

    @PostMapping("/subscribe")
    public String subscribeToClub(@RequestParam UUID clubId, Model model, Principal principal) {
        String email = userService.findByUsername(principal.getName()).getEmail();
        Club club = clubRepo.findById(clubId).orElseThrow();
        ClubSubscriber subscriber = new ClubSubscriber();
        subscriber.setEmail(email);
        subscriber.setClub(club);
        clubSubscriberRepo.save(subscriber);
        return "redirect:/club/" + clubId;
    }

}