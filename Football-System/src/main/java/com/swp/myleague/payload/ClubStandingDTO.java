package com.swp.myleague.payload;

public interface ClubStandingDTO {
    String getClubName();

    Integer getSeason(); // hoặc String nếu bạn định nghĩa theo mùa (2024-2025)

    Integer getTotalPoints();

    String getLogoUrl(); // team logo (custom field)

    Integer getPlayed(); // matches played

    Integer getWins();

    Integer getDraws();

    Integer getLosses();

    Integer getGoalsFor(); // GF

    Integer getGoalsAgainst(); // GA

    Integer getGoalDifference(); // GD

    Integer getPoints(); // Pts

    String getNextLogoUrl(); // opponent logo (optional)
}
