package cl.pedidos360.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Valida que el claim {@code aud} del JWT coincida con la audience configurada.
 */
public class AzureAdAudienceValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_AUDIENCE = new OAuth2Error(
            "invalid_token",
            "El token no contiene una audience válida para Pedidos360",
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
        boolean valid = tokenAudiences.stream().anyMatch(allowedAudiences::contains);

        return valid
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(INVALID_AUDIENCE);
    }
}
