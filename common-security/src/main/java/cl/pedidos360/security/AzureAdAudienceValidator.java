package cl.pedidos360.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public class AzureAdAudienceValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_AUDIENCE = new OAuth2Error(
            "invalid_token",
            "El token no contiene una audience o client_id valido para Pedidos360",
            null
    );

    private final List<String> allowedAudiences;

    public AzureAdAudienceValidator(List<String> allowedAudiences) {
        this.allowedAudiences = allowedAudiences;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (allowedAudiences == null || allowedAudiences.isEmpty()) {
            return OAuth2TokenValidatorResult.success();
        }

        List<String> tokenAudiences = jwt.getAudience();
        boolean validAud = tokenAudiences != null && tokenAudiences.stream().anyMatch(allowedAudiences::contains);
        String clientId = jwt.getClaimAsString("client_id");
        boolean validClientId = clientId != null && allowedAudiences.contains(clientId);

        return (validAud || validClientId)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(INVALID_AUDIENCE);
    }
}
