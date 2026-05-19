package com.example.taxtrusted.service;

import com.example.taxtrusted.model.Lead;
import com.example.taxtrusted.model.Provider;
import com.example.taxtrusted.model.TaxNeed;
import com.example.taxtrusted.repository.ProviderRepository;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProviderMatchingService {
  private final ProviderRepository providerRepository;

  public ProviderMatchingService(ProviderRepository providerRepository) {
    this.providerRepository = providerRepository;
  }

  @Cacheable(value = "providerCandidates", key = "#zipCode + ':' + #needs.toString()")
  public List<Provider> findCandidates(String zipCode, Set<TaxNeed> needs) {
    List<Provider> local = providerRepository.findLocalCandidates(zipCode, List.copyOf(needs));
    if (!local.isEmpty()) {
      return local;
    }
    return needs.stream()
        .flatMap(need -> providerRepository.findActiveByNeed(need).stream())
        .distinct()
        .toList();
  }

  public List<ScoredProvider> match(Lead lead) {
    return findCandidates(lead.getZipCode(), lead.getNeeds()).stream()
        .map(provider -> score(lead, provider))
        .sorted(Comparator.comparingInt(ScoredProvider::score).reversed())
        .limit(3)
        .toList();
  }

  private ScoredProvider score(Lead lead, Provider provider) {
    int score = 0;
    int specialtyMatches = 0;
    for (TaxNeed need : lead.getNeeds()) {
      if (provider.getSpecialties().contains(need)) {
        specialtyMatches++;
      }
    }

    score += specialtyMatches * 35;
    if (provider.getZipCode().equals(lead.getZipCode())) {
      score += 25;
    } else if (provider.getZipCode().substring(0, 3).equals(lead.getZipCode().substring(0, 3))) {
      score += 12;
    }
    score += provider.getRating().multiply(java.math.BigDecimal.TEN).setScale(0, RoundingMode.HALF_UP).intValue();
    score += Math.min(provider.getWeeklyCapacity(), 20);
    score += Math.max(0, 20 - provider.getAverageResponseMinutes() / 60);

    String reason = "%d specialty match%s, %s area, %.1f rating, %d weekly slots"
        .formatted(
            specialtyMatches,
            specialtyMatches == 1 ? "" : "es",
            provider.getZipCode().equals(lead.getZipCode()) ? "same ZIP" : "nearby/serviceable",
            provider.getRating(),
            provider.getWeeklyCapacity()
        );

    return new ScoredProvider(provider, score, reason);
  }

  public record ScoredProvider(Provider provider, int score, String reason) {}
}

