package com.example.taxtrusted.repository;

import com.example.taxtrusted.model.Provider;
import com.example.taxtrusted.model.TaxNeed;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProviderRepository extends JpaRepository<Provider, Long> {
  @EntityGraph(attributePaths = "specialties")
  @Query("""
      select distinct p from Provider p
      join p.specialties s
      where p.active = true
        and s in :needs
        and (
          p.zipCode = :zipCode
          or substring(p.zipCode, 1, 3) = substring(:zipCode, 1, 3)
        )
      """)
  List<Provider> findLocalCandidates(@Param("zipCode") String zipCode, @Param("needs") List<TaxNeed> needs);

  @EntityGraph(attributePaths = "specialties")
  @Query("""
      select distinct p from Provider p
      join p.specialties s
      where p.active = true and s = :need
      order by p.rating desc, p.weeklyCapacity desc
      """)
  List<Provider> findActiveByNeed(@Param("need") TaxNeed need);
}

