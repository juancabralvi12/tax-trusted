package com.example.taxtrusted.controller;

import com.example.taxtrusted.dto.ProviderResponse;
import com.example.taxtrusted.model.TaxNeed;
import com.example.taxtrusted.service.ProviderMatchingService;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {
  private final ProviderMatchingService matchingService;

  public ProviderController(ProviderMatchingService matchingService) {
    this.matchingService = matchingService;
  }

  @GetMapping
  public List<ProviderResponse> providers(
      @RequestParam @Pattern(regexp = "^[0-9]{5}$") String zipCode,
      @RequestParam TaxNeed need
  ) {
    return matchingService.findCandidates(zipCode, Set.of(need)).stream()
        .map(ProviderResponse::from)
        .toList();
  }
}

