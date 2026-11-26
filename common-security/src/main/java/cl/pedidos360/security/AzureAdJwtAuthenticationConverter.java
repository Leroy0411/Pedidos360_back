package cl.pedidos360.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        Collection<GrantedAuthority> authorities = new ArrayList<>(extractRoles(jwt));
        authorities.addAll(scopeConverter.convert(jwt));
        if (authorities.stream().noneMatch(a -> a.getAuthority().startsWith("ROLE_"))) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return new JwtAuthenticationToken(jwt, authorities, resolvePrincipalName(jwt));
    }

    private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        List<GrantedAuthority> result = new ArrayList<>();
        Object roles = jwt.getClaim(properties.getRolesClaim());
        if (roles instanceof List<?> roleList) {
            roleList.forEach(r -> result.add(new SimpleGrantedAuthority("ROLE_" + r.toString().toUpperCase())));
        }
        Object cognitoGroups = jwt.getClaim("cognito:groups");
        if (cognitoGroups instanceof List<?> groupList) {
            groupList.forEach(g -> result.add(new SimpleGrantedAuthority("ROLE_" + g.toString().toUpperCase())));
        }
        return result;
    }

    private String resolvePrincipalName(Jwt jwt) {
        for (String claim : List.of("preferred_username", "email", "cognito:username")) {
            String val = jwt.getClaimAsString(claim);
            if (val != null && !val.isBlank()) {
                return val;
            }
        }
        return jwt.getSubject();
    }
}
