package com.example.taxtrusted.dto;

import com.example.taxtrusted.model.Lead;
import com.example.taxtrusted.model.LeadStatus;
import com.example.taxtrusted.model.TaxNeed;
import com.example.taxtrusted.model.Timeline;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record LeadResponse(
    UUID id,
    String zipCode,
    Set<TaxNeed> needs,
    Timeline timeline,
    LeadStatus status,
    Instant createdAt,
    List<MatchResponse> matches
) {
  public static LeadResponse from(Lead lead, List<MatchResponse> matches) {
    return new LeadResponse(
        lead.getId(),
        lead.getZipCode(),
        lead.getNeeds(),
        lead.getTimeline(),
        lead.getStatus(),
        lead.getCreatedAt(),
        matches
    );
  }
}

