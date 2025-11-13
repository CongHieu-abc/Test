package com.swp.myleague.payload;

import java.util.UUID;

public interface TopAppearedClub {
        UUID getPlayerId();

    String getPlayerFullName();

    String getPlayerImgPath();

    Integer getPlayerNumber();

    String getPlayerNationaly();

    String getClubName();

    Integer getTotalAppeared();
}
