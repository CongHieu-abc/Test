package com.swp.myleague.payload;

import java.util.UUID;

public interface TopScorerClub {
    UUID getPlayerId();

    String getPlayerFullName();

    String getPlayerImgPath();

    Integer getPlayerNumber();

    String getPlayerNationaly();

    String getClubName();

    Integer getTotalGoals();
}
