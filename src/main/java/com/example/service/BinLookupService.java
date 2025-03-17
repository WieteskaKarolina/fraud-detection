package com.example.service;

import com.example.model.BinResponse;
import com.mastercard.developer.interceptors.OkHttpOAuth1Interceptor;
import com.mastercard.developer.utils.AuthenticationUtils;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import okhttp3.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Optional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class BinLookupService {
    private String binApiUrl;
    private String consumerKey;
    private String keyPath;
    private String alias;
    private String password;
    private final OkHttpClient client;

    private static final Logger LOGGER = Logger.getLogger(BinLookupService.class);

    @Inject
    public BinLookupService(
            @ConfigProperty(name = "mastercard.api.url") String binApiUrl,
            @ConfigProperty(name = "mastercard.consumer.key") String consumerKey,
            @ConfigProperty(name = "mastercard.private.key.path") String keyPath,
            @ConfigProperty(name = "mastercard.private.key.alias") String alias,
            @ConfigProperty(name = "mastercard.private.key.password") String password
    ) throws UnrecoverableKeyException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {

        this.binApiUrl = binApiUrl;
        this.consumerKey = consumerKey;
        this.keyPath = keyPath;
        this.alias = alias;
        this.password = password;
        this.client = createClient();
    }


    public BinLookupService(OkHttpClient client) {
        this.client = client;
    }


    public OkHttpClient createClient() throws UnrecoverableKeyException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        PrivateKey signingKey = AuthenticationUtils.loadSigningKey(keyPath, alias, password);
        OkHttpOAuth1Interceptor oauthInterceptor = new OkHttpOAuth1Interceptor(consumerKey, signingKey);

        return new OkHttpClient.Builder()
                .addInterceptor(oauthInterceptor)
                .build();
    }

    @CacheResult(cacheName = "binsCache")
    public Optional<BinResponse> lookupBin(String bin) throws Exception {
        LOGGER.info("BIN not found in cache, calling Mastercard API for: " + bin);
        return callMastercardApi(bin);
    }

    private Optional<BinResponse> callMastercardApi(String bin) throws Exception {
        JSONObject requestBody = new JSONObject();
        requestBody.put("accountRange", bin);

        System.out.println("Sending request to API with BIN: " + bin);
        System.out.println("Request body: " + requestBody.toString(2));

        Request request = new Request.Builder()
                .url(binApiUrl)
                .post(RequestBody.create(MediaType.get("application/json"), requestBody.toString()))
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {

            System.out.println("Response Code: " + response.code());
            String jsonResponse = response.body() != null ? response.body().string() : "";
            System.out.println("Response body: " + jsonResponse);

            if (!response.isSuccessful()) {
                throw new IOException("Request failed: " + response.code() + " - " + response.message());
            }

            if (jsonResponse.isEmpty()) {
                return Optional.empty();
            }

            try {
                JSONArray jsonArray = new JSONArray(jsonResponse);
                if (jsonArray.length() > 0) {
                    return Optional.of(BinResponse.fromJson(jsonArray.getJSONObject(0)));
                } else {
                    return Optional.empty();
                }
            } catch (JSONException e) {
                System.err.println("Error parsing JSON response: " + e.getMessage());
                return Optional.empty();
            }
        }
    }

    public void setBinApiUrl(String binApiUrl) {
        this.binApiUrl = binApiUrl;
    }
}
