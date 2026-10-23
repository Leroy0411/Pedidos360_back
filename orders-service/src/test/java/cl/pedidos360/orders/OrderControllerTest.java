package cl.pedidos360.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
@AutoConfigureMockMvc
class OrderControllerTest {

    @org.springframework.boot.test.mock.mockito.MockBean
    private cl.pedidos360.orders.messaging.OrderEventPublisher eventPublisher;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAndListOrders() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerEmail": "user@test.cl",
                                  "productId": 1,
                                  "quantity": 2,
                                  "totalAmount": 1000.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerEmail").value("user@test.cl"));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerEmail").value("user@test.cl"));
    }
}
