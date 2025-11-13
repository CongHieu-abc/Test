package com.swp.myleague.model.entities.match;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.swp.myleague.model.entities.Comment;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "'match'")
public class Match {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID matchId;

    private String matchTitle;
    private LocalDateTime matchStartTime;
    private String matchDescription;
    private String matchLinkLivestream;

    private String matchReferenceInformation;

    private UUID matchMOM;

    @OneToMany(mappedBy = "match", fetch = FetchType.EAGER)
    private List<MatchEvent> matchEvents;

    @OneToMany(mappedBy = "match", fetch = FetchType.LAZY)
    private List<MatchPlayerStat> matchPlayerStats;

    @OneToMany(mappedBy = "match", fetch = FetchType.EAGER)
    private List<MatchClubStat> matchClubStats;

    @OneToMany(mappedBy = "match", fetch = FetchType.LAZY)
    private List<Comment> comments;

    @Override
    public String toString() {
        return "Match{matchId=" + this.matchId +
                ", matchStartTime=" + this.matchStartTime +
                ", statsCount=" + (matchClubStats != null ? matchClubStats.size() : 0) +
                "}";
    }

    @Transient
    private int totalTickets;

    @Transient
    private int soldTickets;

}
