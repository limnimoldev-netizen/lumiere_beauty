package com.example.backend_lumiere;

import com.example.backend_lumiere.model.Product;
import com.example.backend_lumiere.repository.OrderItemRepository;
import com.example.backend_lumiere.repository.OrderRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:order_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false"
})
class OrderApiTests {
    @Autowired WebApplicationContext context;
    @Autowired ProductRepository products;
    @Autowired OrderRepository orders;
    @Autowired OrderItemRepository orderItems;

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
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].items").doesNotExist());

        Long orderId = orders.findAll().getFirst().getId();
        Long orderItemId = orderItems.findAll().getFirst().getId();

        mvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.items").doesNotExist());

        mvc.perform(get("/api/order-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].product.id").value(product.getId()));

        mvc.perform(get("/api/order-items/" + orderItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.id").value(product.getId()));
    }
}
