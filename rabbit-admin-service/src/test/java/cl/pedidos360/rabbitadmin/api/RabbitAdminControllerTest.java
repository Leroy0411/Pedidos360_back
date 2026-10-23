package cl.pedidos360.rabbitadmin.api;

import cl.pedidos360.rabbitadmin.service.RabbitAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import cl.pedidos360.rabbitadmin.config.AdminSecurity;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RabbitAdminController.class)
@Import(AdminSecurity.class)
class RabbitAdminControllerTest {
    @Autowired MockMvc mvc;
    @MockBean RabbitAdminService service;

    @Test void emptyQueueNameIsRejected() throws Exception {
        mvc.perform(post("/api/admin/queues").with(httpBasic("admin", "change-this-before-deploy"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test void unauthenticatedAdministrationIsRejected() throws Exception {
        mvc.perform(post("/api/admin/queues").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test.queue\"}"))
                .andExpect(status().isUnauthorized());
    }
}
