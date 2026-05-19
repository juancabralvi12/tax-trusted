package com.example.taxtrusted.service;

import com.example.taxtrusted.dto.CreateLeadRequest;
import com.example.taxtrusted.dto.LeadResponse;
import com.example.taxtrusted.dto.MatchResponse;
import com.example.taxtrusted.model.Lead;
import com.example.taxtrusted.model.LeadMatch;
import com.example.taxtrusted.repository.LeadMatchRepository;
import com.example.taxtrusted.repository.LeadRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeadService {
  private final LeadRepository leadRepository;
  private final LeadMatchRepository leadMatchRepository;
  private final ProviderMatchingService matchingService;

  public LeadService(LeadRepository leadRepository, LeadMatchRepository leadMatchRepository, ProviderMatchingService matchingService) {
    this.leadRepository = leadRepository;
    this.leadMatchRepository = leadMatchRepository;
    this.matchingService = matchingService;
  }

  @Transactional
  public LeadResponse create(CreateLeadRequest request) {
    Lead lead = new Lead(
        request.zipCode(),
        request.needs(),
        request.timeline(),
        request.firstName(),
        request.lastName(),
        request.email(),
        request.phone()
    );

    leadRepository.save(lead);

    List<ProviderMatchingService.ScoredProvider> scoredProviders = matchingService.match(lead);
    int rank = 1;
    for (ProviderMatchingService.ScoredProvider scoredProvider : scoredProviders) {
      leadMatchRepository.save(new LeadMatch(lead, scoredProvider.provider(), scoredProvider.score(), rank++, scoredProvider.reason()));
    }

    if (!scoredProviders.isEmpty()) {
      lead.markMatched();
    }

    List<MatchResponse> matches = leadMatchRepository.findByLeadIdOrderByRankAsc(lead.getId()).stream()
        .map(MatchResponse::from)
        .toList();

    return LeadResponse.from(lead, matches);
  }

  @Transactional(readOnly = true)
  public LeadResponse get(UUID id) {
    Lead lead = leadRepository.findById(id).orElseThrow();
    List<MatchResponse> matches = leadMatchRepository.findByLeadIdOrderByRankAsc(id).stream()
        .map(MatchResponse::from)
        .toList();
    return LeadResponse.from(lead, matches);
  }
}

