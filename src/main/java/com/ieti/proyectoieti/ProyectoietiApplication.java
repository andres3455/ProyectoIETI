package com.ieti.proyectoieti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProyectoietiApplication {

  /** Private constructor to prevent instantiation. */
  private ProyectoietiApplication() {
    // Utility class pattern
  }

  public static void main(String[] args) {
    SpringApplication.run(ProyectoietiApplication.class, args);
    System.out.println("EventIA is running...");
  }
}
