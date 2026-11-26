package cl.pedidos360.bff.controller;

import cl.pedidos360.bff.service.DownstreamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderBffController {

    private final DownstreamService downstreamService;

    public OrderBffController(DownstreamService downstreamService) {
        this.downstreamService = downstreamService;
    }

    private String resolveEmail(Jwt jwt) {
        for (String claim : List.of("preferred_username", "email", "cognito:username")) {
            String val = jwt.getClaimAsString(claim);
            if (val != null && !val.isBlank()) return val;
        }
        return jwt.getSubject();
    }

    private boolean isAdmin(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        List<String> groups = jwt.getClaimAsStringList("cognito:groups");
        return (roles != null && roles.stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN")))
                || (groups != null && groups.stream().anyMatch(g -> g.equalsIgnoreCase("ADMIN")));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Map<String, Object>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String customerEmail
    ) {
        String emailFilter = isAdmin(jwt) ? customerEmail : resolveEmail(jwt);
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
        body.put("customerEmail", resolveEmail(jwt));
        return downstreamService.createOrder(body);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> confirm(@PathVariable Long id) {
        return downstreamService.confirmOrder(id);
    }
}
