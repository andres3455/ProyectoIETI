package com.ieti.proyectoieti.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Tag(name = "Login", description = "Login page APIs")
public class LoginController {

    @Operation(
            summary = "Login page",
            description = "Redirects to the OAuth2 login page"
    )
    @ApiResponse(responseCode = "200", description = "Login page displayed")
    @GetMapping("/login")
    public String login() {
        return "redirect:/oauth2/authorization/google";
    }

    @Operation(
            summary = "Home page",
            description = "Application home page"
    )
    @ApiResponse(responseCode = "200", description = "Home page displayed")
    @GetMapping("/")
    public String home() {
        return "home";
    }
}