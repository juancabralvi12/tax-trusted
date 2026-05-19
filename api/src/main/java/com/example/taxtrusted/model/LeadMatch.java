package com.example.taxtrusted.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "lead_matches")
public class LeadMatch implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lead_id")
  private Lead lead;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "provider_id")
  private Provider provider;

  private int score;
  private int rank;
  private String scoreReason;
  private Instant createdAt;

  protected LeadMatch() {}

  public LeadMatch(Lead lead, Provider provider, int score, int rank, String scoreReason) {
    this.lead = lead;
    this.provider = provider;
    this.score = score;
    this.rank = rank;
    this.scoreReason = scoreReason;
    this.createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public Lead getLead() { return lead; }
  public Provider getProvider() { return provider; }
  public int getScore() { return score; }
  public int getRank() { return rank; }
  public String getScoreReason() { return scoreReason; }
  public Instant getCreatedAt() { return createdAt; }
}
