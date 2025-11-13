package com.swp.myleague.model.entities.information;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.swp.myleague.model.entities.match.MatchEvent;
import com.swp.myleague.model.entities.match.MatchPlayerStat;

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
public class Player {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID playerId;
    
    private String playerFullName;
    private Integer playerNumber;
    private String playerImgPath;
    private String playerInformation;
    private String playerNationaly;
    private Integer playerScores;
    private Integer playerAssist;
    private Integer playerAppearances;
    private Integer playerCleanSheets;

    @Enumerated(EnumType.STRING)
    private PlayerPosition playerPosition;

    @ManyToOne
    @JoinColumn(name = "clubId")
    private Club club;

    @OneToMany(mappedBy = "player", fetch = FetchType.LAZY)
    private List<MatchEvent> matchEvents;

    @OneToMany(mappedBy = "player", fetch = FetchType.LAZY)
    private List<MatchPlayerStat> matchPlayerStats;

}
