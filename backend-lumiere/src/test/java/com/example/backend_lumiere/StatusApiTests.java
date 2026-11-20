package com.example.backend_lumiere;

import com.example.backend_lumiere.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:status_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class StatusApiTests {
    @Autowired WebApplicationContext context;
    @Autowired UserRepository users;

    @Test
    void userApiWorksWithoutAuthentication() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();

        mvc.perform(get("/api/users")).andExpect(status().isOk());
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test User\",\"email\":\"test-user@example.com\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist());
        Long id = users.findByEmail("test-user@example.com").orElseThrow().getId();

        mvc.perform(get("/api/users").header("Authorization", "Basic invalid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'test-user@example.com')].active").value(true));
        mvc.perform(get("/api/users/" + id)).andExpect(status().isOk());
        mvc.perform(patch("/api/users/" + id + "/role")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN"));
        mvc.perform(patch("/api/users/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        mvc.perform(options("/api/users/" + id + "/status")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(patch("/api/users/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/users/" + id)).andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertFalse(users.existsById(id));
    }

    @Test
    void managedUserCreationNeedsNoAdminSession() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        mvc.perform(post("/api/users/admin").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Manager\",\"email\":\"manager@example.com\",\"password\":\"test-password\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
