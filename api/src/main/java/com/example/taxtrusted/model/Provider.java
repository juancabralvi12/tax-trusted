package com.example.taxtrusted.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "providers")
public class Provider implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String firmName;
  private String zipCode;
  private String city;
  private String state;

  @Column(columnDefinition = "text")
  private String bio;

  private BigDecimal rating;
  private int averageResponseMinutes;
  private int weeklyCapacity;
  private boolean active;
  private Instant createdAt;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "provider_specialties", joinColumns = @JoinColumn(name = "provider_id"))
  @Column(name = "specialty")
  @Enumerated(EnumType.STRING)
  private Set<TaxNeed> specialties = new HashSet<>();

  public Long getId() { return id; }
  public String getName() { return name; }
  public String getFirmName() { return firmName; }
  public String getZipCode() { return zipCode; }
  public String getCity() { return city; }
  public String getState() { return state; }
  public String getBio() { return bio; }
  public BigDecimal getRating() { return rating; }
  public int getAverageResponseMinutes() { return averageResponseMinutes; }
  public int getWeeklyCapacity() { return weeklyCapacity; }
  public boolean isActive() { return active; }
  public Instant getCreatedAt() { return createdAt; }
  public Set<TaxNeed> getSpecialties() { return specialties; }
}
