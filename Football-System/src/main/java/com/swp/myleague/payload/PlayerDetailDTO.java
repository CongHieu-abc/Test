package com.swp.myleague.payload;

import java.util.UUID;

public interface PlayerDetailDTO {
    UUID getPlayerId();
    String getTotalAssists();
    String getCleanSheets();
    String getTotalGoals();
    String getTotalMinutes();
}
