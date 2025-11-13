package com.swp.myleague.payload;

import java.util.UUID;


public interface CareerPlayer {
    UUID getPlayerId();
    UUID getClubId();
    String getClubName();
    Integer getAppeared();
    Integer getGoals();
    Integer getSeason();
}
