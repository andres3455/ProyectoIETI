package com.ieti.proyectoieti.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.autoconfigure.exclude=org.springdoc.core.SpringDocConfiguration,org.springdoc.webmvc.ui.SwaggerConfig",
        "springdoc.api-docs.enabled=false",
        "springdoc.swagger-ui.enabled=false",
        "spring.security.oauth2.client.registration.google.client-id=test",
        "spring.security.oauth2.client.registration.google.client-secret=test"
})
class SecurityConfigTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void healthEndpoint_ShouldBePublic() throws Exception {
    mockMvc.perform(get("/health"))
            .andExpect(status().isOk());
  }

  @Test
  void loginEndpoint_ShouldRedirect() throws Exception {
    mockMvc.perform(get("/login"))
            .andExpect(status().is3xxRedirection());
  }

  @Test
  void authStatus_ShouldBePublic() throws Exception {
    mockMvc.perform(get("/api/auth/status"))
            .andExpect(status().isOk());
  }

  @Test
  void securedEndpoints_ShouldRequireAuthentication() throws Exception {
    mockMvc.perform(get("/api/wallets"))
            .andExpect(status().is3xxRedirection());

    mockMvc.perform(get("/events"))
            .andExpect(status().is3xxRedirection());
  }
}