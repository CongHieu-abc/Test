package com.swp.myleague.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp.myleague.model.entities.User;
import com.swp.myleague.model.entities.ticket.Ticket;
import com.swp.myleague.model.service.EmailService;
import com.swp.myleague.model.service.UserService;
import com.swp.myleague.model.service.matchservice.MatchService;
import com.swp.myleague.model.service.ticketservice.TicketService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequestMapping(value = { "/ticket", "/ticket/" })
public class TicketController {

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.payUrl}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @Autowired
    MatchService matchService;

    @Autowired
    TicketService ticketService;

    @Autowired
    EmailService emailService;

    @Autowired
    UserService userService;

    @GetMapping("/{matchId}")
    public String getTicketByMatchId(@PathVariable(name = "matchId") String matchId, Model model)
            throws JsonProcessingException {
        model.addAttribute("match", matchService.getById(matchId));
        List<Ticket> tickets = ticketService.getByMatchId(matchId);

        ObjectMapper mapper = new ObjectMapper();
        String ticketsJson = mapper.writeValueAsString(ticketService.getByMatchId(matchId));

        model.addAttribute("ticketsJson", ticketsJson);

        Map<String, SeatStats> areaDetails = new HashMap<>();
        for (Ticket ticket : tickets) {
            String area = ticket.getTicketArea().toString();
            String type = ticket.getTicketType().toString();
            long amount = ticket.getTicketAmount();

            areaDetails.putIfAbsent(area, new SeatStats());

            SeatStats stats = areaDetails.get(area);
            if ("VIP".equalsIgnoreCase(type)) {
                stats.setVip(stats.getVip() + amount);
            } else {
                stats.setStandard(stats.getStandard() + amount);
            }
        }

        model.addAttribute("areaDetails", areaDetails);

        return "Ticket";
    }

    @PostMapping("/bookingticket")
    public String postMethodName(HttpServletRequest req, @RequestParam(name = "ticketId") String ticketId, Model model,
            Principal principal)
            throws Exception {
        User user = userService.findByUsername(principal.getName());
        Ticket ticket = ticketService.getById(ticketId);
        Double amount = ticket.getTicketPrice();

        return "redirect:/payment/create-payment?amount=" + amount + "&orderInfo=Ticket:" + ticketId + "&email="
                + user.getEmail();
    }

}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class SeatStats {
    private long vip;
    private long standard;
}
