package com.example.client;

import com.example.dto.BinRequest;
import com.example.model.BinResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import com.example.auth.MastercardAuthFilter;

import java.util.List;

@RegisterRestClient(baseUri = "https://sandbox.api.mastercard.com")
@RegisterProvider(MastercardAuthFilter.class)
@Path("/bin-resources/bin-ranges/account-searches")
public interface BinLookupRestClient {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    List<BinResponse> lookupBin(BinRequest request);
}

