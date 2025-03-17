package com.example.service;

import com.example.model.BinResponse;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class BinLookupServiceIntegrationTest {

    @Test
    void testLookupBinWithConfig() throws UnrecoverableKeyException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        // Load the configuration from application.properties
        Config config = ConfigProvider.getConfig();

        // Read configuration values
        String binApiUrl = config.getValue("mastercard.api.url", String.class);
        String consumerKey = config.getValue("mastercard.consumer.key", String.class);
        String keyPath = config.getValue("mastercard.private.key.path", String.class);
        String alias = config.getValue("mastercard.private.key.alias", String.class);
        String password = config.getValue("mastercard.private.key.password", String.class);

        // Initialize BinLookupService with config values
        BinLookupService binLookupService = new BinLookupService(binApiUrl, consumerKey, keyPath, alias, password);

        // BIN number to test
        String bin = "585240";  // Adjust to test a valid BIN

        try {
            // Call the lookupBin() method
            Optional<BinResponse> resultOpt = binLookupService.lookupBin(bin);

            // Check that the result is present and matches the expected values
            assertTrue(resultOpt.isPresent(), "BinResponse should be present");
            BinResponse result = resultOpt.get();
            assertEquals("585240", result.getBinNum());
            assertEquals(6, result.getBinLength());
            assertEquals("Poland", result.getCountry().getName());
            assertEquals("KOHO FInullCIAL INCORPORATED", result.getCustomerName());
            assertEquals("CIR", result.getAcceptanceBrand());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testLookupBinWithNoContent() throws UnrecoverableKeyException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        // Load the configuration from application.properties
        Config config = ConfigProvider.getConfig();

        // Read configuration values
        String binApiUrl = config.getValue("mastercard.api.url", String.class);
        String consumerKey = config.getValue("mastercard.consumer.key", String.class);
        String keyPath = config.getValue("mastercard.private.key.path", String.class);
        String alias = config.getValue("mastercard.private.key.alias", String.class);
        String password = config.getValue("mastercard.private.key.password", String.class);

        // Initialize BinLookupService with config values
        BinLookupService binLookupService = new BinLookupService(binApiUrl, consumerKey, keyPath, alias, password);

        // BIN number that will not return data (adjust this to test a BIN without a response)
        String bin = "000000";  // Example of a BIN that does not exist or return any data

        try {
            // Call the lookupBin() method
            Optional<BinResponse> resultOpt = binLookupService.lookupBin(bin);

            // Assert that the result is empty (204 No Content scenario)
            assertTrue(resultOpt.isEmpty(), "BinResponse should be empty for a BIN with no data");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred: " + e.getMessage());
        }
    }
}
