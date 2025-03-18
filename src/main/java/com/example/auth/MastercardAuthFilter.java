package com.example.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastercard.developer.oauth.OAuth;
import com.mastercard.developer.utils.AuthenticationUtils;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Collections;

@ApplicationScoped
@RegisterProvider(MastercardAuthFilter.class)
@Priority(Priorities.AUTHENTICATION)
public class MastercardAuthFilter implements ClientRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(MastercardAuthFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PrivateKey signingKey;
    private final String consumerKey;

    @Inject
    public MastercardAuthFilter(
            @ConfigProperty(name = "mastercard.private.key.path") String privateKeyPath,
            @ConfigProperty(name = "mastercard.private.key.alias") String privateKeyAlias,
            @ConfigProperty(name = "mastercard.private.key.password") String privateKeyPassword,
            @ConfigProperty(name = "mastercard.consumer.key") String consumerKey
    ) {
        this.consumerKey = consumerKey;

        try {
            this.signingKey = AuthenticationUtils.loadSigningKey(privateKeyPath, privateKeyAlias, privateKeyPassword);
        } catch (Exception e) {
            throw new RuntimeException("Error loading Mastercard private key", e);
        }
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        try {
            URI uri = requestContext.getUri();
            String method = requestContext.getMethod();
            String body = serializeRequestBody(requestContext);

            String authHeader = OAuth.getAuthorizationHeader(
                    uri, method, body, StandardCharsets.UTF_8, consumerKey, signingKey
            );

            requestContext.getHeaders().put(HttpHeaders.AUTHORIZATION, Collections.singletonList(authHeader));

        } catch (Exception e) {
            LOGGER.error("OAuth authentication error", e);
            throw new IOException("OAuth signing error", e);
        }
    }

    private String serializeRequestBody(ClientRequestContext requestContext) {
        if (requestContext.hasEntity()) {
            try {
                return objectMapper.writeValueAsString(requestContext.getEntity());
            } catch (Exception e) {
                LOGGER.error("JSON serialization error", e);
            }
        }
        return "";
    }
}
