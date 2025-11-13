package com.swp.myleague.model.entities.match;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.swp.myleague.model.entities.information.Club;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class MatchClubStat {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID matchClubStatId;
    
    private Integer matchClubStatYellowCard;
    private Integer matchClubStatRedCard;
    private Integer matchClubStatShoots;
    private Integer matchClubStatPass;
    private Integer matchClubStatCorners;
    private Integer matchClubStatBallTimes;
    private Integer matchClubStatScore;

    @ManyToOne
    @JoinColumn(name = "matchId")
    private Match match;

    @ManyToOne
    @JoinColumn(name = "clubId")
    private Club club;

}
