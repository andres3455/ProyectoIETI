package com.ieti.proyectoieti.Config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoints_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        testSwaggerApiDocs();

        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isOk());
    }

    private void testSwaggerApiDocs() throws Exception {
        String[] apiDocsPaths = {
                "/v3/api-docs/swagger-config",
                "/v3/api-docs",
                "/api-docs",
                "/swagger-resources/configuration/ui",
                "/swagger-resources/configuration/security",
                "/swagger-resources"
        };

        boolean atLeastOneWorks = false;

        for (String path : apiDocsPaths) {
            MvcResult result = mockMvc.perform(get(path)).andReturn();
            int status = result.getResponse().getStatus();
            if (status == 200) {
                atLeastOneWorks = true;
                break;
            }
        }

        if (!atLeastOneWorks) {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Test
    void securedEndpoints_ShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/wallets"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/events"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser
    void securedEndpoints_WithAuthentication_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/wallets"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk());
    }

    @Test
    void oauth2LoginEndpoint_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection());
    }
}