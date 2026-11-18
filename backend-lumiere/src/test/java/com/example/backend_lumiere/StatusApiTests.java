package com.example.backend_lumiere;

import com.example.backend_lumiere.model.User;
import com.example.backend_lumiere.repository.UserRepository;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:status_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class StatusApiTests {
    @Autowired WebApplicationContext context;
    @Autowired Filter springSecurityFilterChain;
    @Autowired UserRepository users;

    @Test
    void adminCanDisableAndReactivateAnotherAccount() throws Exception {
        User customer = new User();
        customer.setName("Status Customer");
        customer.setEmail("status-customer@example.com");
        customer.setPassword("test-password");
        customer.setRole("customer");
        customer.setActive(null); // Old rows with no status remain active.
        users.save(customer);

        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        MockHttpSession adminSession = (MockHttpSession) mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@gmail.com\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
        mvc.perform(get("/api/auth/me").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@gmail.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mvc.perform(get("/api/users").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'status-customer@example.com')].active").value(true));

        mvc.perform(patch("/api/users/" + customer.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(options("/api/users/" + customer.getId() + "/status")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(patch("/api/users/" + customer.getId() + "/status").session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/users/" + customer.getId() + "/status").session(adminSession)
                        .header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
        org.junit.jupiter.api.Assertions.assertFalse(users.findById(customer.getId()).orElseThrow().isActive());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"status-customer@example.com\",\"password\":\"test-password\"}"))
                .andExpect(status().isUnauthorized());

        Long adminId = users.findByEmail("admin@gmail.com").orElseThrow().getId();
        mvc.perform(patch("/api/users/" + adminId + "/status").session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/users/" + customer.getId() + "/status").session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(true));
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"status-customer@example.com\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk());
    }
}
