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
        Config config = ConfigProvider.getConfig();

        String binApiUrl = config.getValue("mastercard.api.url", String.class);
        String consumerKey = config.getValue("mastercard.consumer.key", String.class);
        String keyPath = config.getValue("mastercard.private.key.path", String.class);
        String alias = config.getValue("mastercard.private.key.alias", String.class);
        String password = config.getValue("mastercard.private.key.password", String.class);

        BinLookupService binLookupService = new BinLookupService(binApiUrl, consumerKey, keyPath, alias, password);

        String bin = "585240";

        try {
            Optional<BinResponse> resultOpt = binLookupService.lookupBin(bin);

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
        Config config = ConfigProvider.getConfig();

        String binApiUrl = config.getValue("mastercard.api.url", String.class);
        String consumerKey = config.getValue("mastercard.consumer.key", String.class);
        String keyPath = config.getValue("mastercard.private.key.path", String.class);
        String alias = config.getValue("mastercard.private.key.alias", String.class);
        String password = config.getValue("mastercard.private.key.password", String.class);

        BinLookupService binLookupService = new BinLookupService(binApiUrl, consumerKey, keyPath, alias, password);

        String bin = "000000";

        try {
            Optional<BinResponse> resultOpt = binLookupService.lookupBin(bin);

            assertTrue(resultOpt.isEmpty(), "BinResponse should be empty for a BIN with no data");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred: " + e.getMessage());
        }
    }
}
