package com.swp.myleague.payload;

import java.util.UUID;

public interface ClubStatOverview {
    UUID getClubId();
    Integer getAllGoals();
    Integer getAllAssists();
}
