package cl.pedidos360.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "pedidos360.security.jwt")
public class AzureAdJwtProperties {

    /**
     * Issuer esperado del token (Azure AD v2).
     * Ejemplo: https://login.microsoftonline.com/{tenant-id}/v2.0
     */
    private String issuerUri;

    /**
     * Audience(s) válidos. En Azure AD suele ser api://{client-id} o el client-id.
     */
    private List<String> audiences = new ArrayList<>();

    /**
     * Claim donde vienen los roles de la aplicación.
     */
    private String rolesClaim = "roles";

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public List<String> getAudiences() {
        return audiences;
    }

    public void setAudiences(List<String> audiences) {
        this.audiences = audiences;
    }

    public String getRolesClaim() {
        return rolesClaim;
    }

    public void setRolesClaim(String rolesClaim) {
        this.rolesClaim = rolesClaim;
    }
}
