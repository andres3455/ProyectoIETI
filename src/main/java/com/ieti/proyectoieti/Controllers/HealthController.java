package com.ieti.proyectoieti.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Health", description = "Health check API")
public class HealthController {

  @Operation(
          summary = "Health check",
          description = "Returns the health status of the service"
  )
  @ApiResponse(responseCode = "200", description = "Service is healthy")
  @GetMapping("/health")
  public Map<String, String> healthCheck() {
    return Map.of("status", "OK",
            "message", "Service is running"
    );
  }
}