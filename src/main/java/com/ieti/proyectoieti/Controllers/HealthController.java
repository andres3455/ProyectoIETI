package com.ieti.proyectoieti.Controllers;

import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HealthController {
  @GetMapping("/health")
  public Map<String, String> healthCheck() {
    return Map.of("status", "OK",
        "message", "Service is running"
    );
  }  

}
