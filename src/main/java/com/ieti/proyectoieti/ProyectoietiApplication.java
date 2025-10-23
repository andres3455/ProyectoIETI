package com.ieti.proyectoieti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class ProyectoietiApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProyectoietiApplication.class, args);
    System.out.println("EventIA is running...");
  }
}
