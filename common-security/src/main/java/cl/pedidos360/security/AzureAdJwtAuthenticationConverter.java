package cl.pedidos360.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convierte el JWT de Azure AD en un Authentication con roles del claim configurado.
 */
public class AzureAdJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final AzureAdJwtProperties properties;
    private final JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

    public AzureAdJwtAuthenticationConverter(AzureAdJwtProperties properties) {
        this.properties = properties;
        scopeConverter.setAuthorityPrefix("SCOPE_");
        scopeConverter.setAuthoritiesClaimName("scp");
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractRoles(jwt);
        authorities.addAll(scopeConverter.convert(jwt));

        return new JwtAuthenticationToken(jwt, authorities, resolvePrincipalName(jwt));
    }

    private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        Object roles = jwt.getClaim(properties.getRolesClaim());
        if (roles instanceof List<?> roleList) {
            return roleList.stream()
                    .map(Object::toString)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String resolvePrincipalName(Jwt jwt) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }
        return jwt.getSubject();
    }
}
