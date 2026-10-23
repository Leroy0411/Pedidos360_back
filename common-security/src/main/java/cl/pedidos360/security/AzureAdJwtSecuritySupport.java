package cl.pedidos360.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
@EnableConfigurationProperties(AzureAdJwtProperties.class)
public class AzureAdJwtSecuritySupport {

    @Bean
    @Profile("!test")
    public JwtDecoder jwtDecoder(AzureAdJwtProperties properties) {
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(properties.getIssuerUri());

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(properties.getIssuerUri());
        OAuth2TokenValidator<Jwt> withAudience = new AzureAdAudienceValidator(properties.getAudiences());
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience);

        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean
    public AzureAdJwtAuthenticationConverter azureAdJwtAuthenticationConverter(AzureAdJwtProperties properties) {
        return new AzureAdJwtAuthenticationConverter(properties);
    }
}
