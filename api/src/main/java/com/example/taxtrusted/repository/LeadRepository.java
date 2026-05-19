package com.example.taxtrusted.repository;

import com.example.taxtrusted.model.Lead;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, UUID> {}

