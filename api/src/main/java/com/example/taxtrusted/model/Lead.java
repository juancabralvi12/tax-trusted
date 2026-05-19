package com.example.taxtrusted.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "leads")
public class Lead implements Serializable {
  @Id
  private UUID id;

  private String zipCode;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;

  @Enumerated(EnumType.STRING)
  private Timeline timeline;

  @Enumerated(EnumType.STRING)
  private LeadStatus status;

  private Instant createdAt;

  @ElementCollection
  @CollectionTable(name = "lead_needs", joinColumns = @JoinColumn(name = "lead_id"))
  @Column(name = "need")
  @Enumerated(EnumType.STRING)
  private Set<TaxNeed> needs = new HashSet<>();

  protected Lead() {}

  public Lead(String zipCode, Set<TaxNeed> needs, Timeline timeline, String firstName, String lastName, String email, String phone) {
    this.id = UUID.randomUUID();
    this.zipCode = zipCode;
    this.needs = needs;
    this.timeline = timeline;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phone = phone;
    this.status = LeadStatus.NEW;
    this.createdAt = Instant.now();
  }

  public UUID getId() { return id; }
  public String getZipCode() { return zipCode; }
  public String getFirstName() { return firstName; }
  public String getLastName() { return lastName; }
  public String getEmail() { return email; }
  public String getPhone() { return phone; }
  public Timeline getTimeline() { return timeline; }
  public LeadStatus getStatus() { return status; }
  public Instant getCreatedAt() { return createdAt; }
  public Set<TaxNeed> getNeeds() { return needs; }

  public void markMatched() {
    this.status = LeadStatus.MATCHED;
  }
}
