package com.example.taxtrusted;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TaxTrustedApplication {
  public static void main(String[] args) {
    SpringApplication.run(TaxTrustedApplication.class, args);
  }
}

