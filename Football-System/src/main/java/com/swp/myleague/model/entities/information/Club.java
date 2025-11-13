package com.swp.myleague.model.entities.information;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.swp.myleague.model.entities.blog.Blog;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"players", "blogs"})
@Entity
public class Club {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID clubId;

    private String clubName;
    private String clubLogoPath;
    private String clubDescription;
    private String clubPrimaryColor;
    private String clubSecondaryColor;
    private String clubFounded;
    private String clubHomeKit;
    private String clubAwayKit;
    private String clubThirdKit;
    private Boolean isActive;

    private String clubStadium;
    private Integer clubStadiumCapacity;

    @OneToMany(mappedBy = "club", fetch = FetchType.LAZY)
    private List<Player> players;

    @OneToMany(mappedBy = "club", fetch = FetchType.LAZY)
    private List<Blog> blogs;

    private UUID userId;

}
