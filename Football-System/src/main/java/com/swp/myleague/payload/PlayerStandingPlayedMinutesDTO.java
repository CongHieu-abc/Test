package com.swp.myleague.payload;

import java.util.UUID;

public interface PlayerStandingPlayedMinutesDTO {
    UUID getPlayerId();
    String getTotalMinutes();
}
