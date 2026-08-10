package com.sujal.itsm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // ✅ Add this
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableAsync       // ✅ Add this
@EnableScheduling  // ✅ Add this (for Step 2 scheduled jobs)
@EnableAspectJAutoProxy
@EnableCaching
public class TicketsystemApplication {
  public static void main(String[] args) {
    SpringApplication.run(TicketsystemApplication.class, args);
  }
}