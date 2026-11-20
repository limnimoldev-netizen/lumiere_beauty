package com.example.backend_lumiere;

import com.example.backend_lumiere.model.Product;
import com.example.backend_lumiere.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:order_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrderApiTests {
    @Autowired WebApplicationContext context;
    @Autowired ProductRepository products;

    @Test
    void createsAndReadsOrderOnFreshDatabase() throws Exception {
        Product product = new Product();
        product.setName("Test product");
        product = products.save(product);

        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"items\":[{\"product\":{\"id\":" + product.getId()
                                + "},\"quantity\":2,\"unitPrice\":100}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].subtotal").value(200));

        mvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].items[0].product.id").value(product.getId()));
    }
}
