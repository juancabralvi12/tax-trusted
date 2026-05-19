package com.example.taxtrusted.controller;

import com.example.taxtrusted.dto.CreateLeadRequest;
import com.example.taxtrusted.dto.LeadResponse;
import com.example.taxtrusted.service.LeadService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leads")
public class LeadController {
  private final LeadService leadService;

  public LeadController(LeadService leadService) {
    this.leadService = leadService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public LeadResponse create(@Valid @RequestBody CreateLeadRequest request) {
    return leadService.create(request);
  }

  @GetMapping("/{id}")
  public LeadResponse get(@PathVariable UUID id) {
    return leadService.get(id);
  }
}

