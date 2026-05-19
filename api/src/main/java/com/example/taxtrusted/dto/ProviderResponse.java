package com.example.taxtrusted.dto;

import com.example.taxtrusted.model.Provider;
import com.example.taxtrusted.model.TaxNeed;
import java.math.BigDecimal;
import java.util.Set;

public record ProviderResponse(
    Long id,
    String name,
    String firmName,
    String city,
    String state,
    String zipCode,
    String bio,
    BigDecimal rating,
    int averageResponseMinutes,
    int weeklyCapacity,
    Set<TaxNeed> specialties
) {
  public static ProviderResponse from(Provider provider) {
    return new ProviderResponse(
        provider.getId(),
        provider.getName(),
        provider.getFirmName(),
        provider.getCity(),
        provider.getState(),
        provider.getZipCode(),
        provider.getBio(),
        provider.getRating(),
        provider.getAverageResponseMinutes(),
        provider.getWeeklyCapacity(),
        provider.getSpecialties()
    );
  }
}

