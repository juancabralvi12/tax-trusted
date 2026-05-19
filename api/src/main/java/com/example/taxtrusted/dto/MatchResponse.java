package com.example.taxtrusted.dto;

import com.example.taxtrusted.model.LeadMatch;

public record MatchResponse(
    int rank,
    int score,
    String scoreReason,
    ProviderResponse provider
) {
  public static MatchResponse from(LeadMatch match) {
    return new MatchResponse(
        match.getRank(),
        match.getScore(),
        match.getScoreReason(),
        ProviderResponse.from(match.getProvider())
    );
  }
}

