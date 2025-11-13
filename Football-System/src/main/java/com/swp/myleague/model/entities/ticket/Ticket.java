package com.swp.myleague.model.entities.ticket;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swp.myleague.model.entities.match.Match;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Ticket {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID ticketId;

    private String ticketTitle;
    private Double ticketPrice;
    private Integer ticketAmount;

    @Enumerated(EnumType.STRING)
    private TicketType ticketType;

    @Enumerated(EnumType.STRING)
    private TicketArea ticketArea;

    @ManyToOne
    @JoinColumn(name = "matchId")
    @JsonIgnore
    private Match match;

    @OneToMany(mappedBy = "ticket", fetch = FetchType.LAZY)
    private List<UserTicket> userTickets;

}
