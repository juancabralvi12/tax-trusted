package com.example.taxtrusted.repository;

import com.example.taxtrusted.model.LeadMatch;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadMatchRepository extends JpaRepository<LeadMatch, Long> {
  List<LeadMatch> findByLeadIdOrderByRankAsc(UUID leadId);
}

