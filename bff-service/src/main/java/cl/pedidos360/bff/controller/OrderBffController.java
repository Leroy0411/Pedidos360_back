package cl.pedidos360.bff.controller;

import cl.pedidos360.bff.service.DownstreamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderBffController {

    private final DownstreamService downstreamService;

    public OrderBffController(DownstreamService downstreamService) {
        this.downstreamService = downstreamService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Map<String, Object>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String customerEmail
    ) {
        boolean isAdmin = jwt.getClaimAsStringList("roles") != null
                && jwt.getClaimAsStringList("roles").contains("ADMIN");

        String emailFilter = isAdmin ? customerEmail : jwt.getClaimAsString("preferred_username");
        return downstreamService.listOrders(emailFilter);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Map<String, Object> getById(@PathVariable Long id) {
        return downstreamService.getOrder(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Map<String, Object> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, Object> body
    ) {
        body.put("customerEmail", jwt.getClaimAsString("preferred_username"));
        return downstreamService.createOrder(body);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> confirm(@PathVariable Long id) {
        return downstreamService.confirmOrder(id);
    }
}
