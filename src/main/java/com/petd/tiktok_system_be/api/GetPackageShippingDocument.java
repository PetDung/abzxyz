package com.petd.tiktok_system_be.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.appClient.RequestClient;
import com.petd.tiktok_system_be.shared.TiktokCallApi;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetPackageShippingDocument implements TiktokCallApi {

    private final String api = "/fulfillment/202309/packages/{package_id}/shipping_documents";

    RequestClient requestClient;

    String packageId;

    String shopCipher;
    String accessToken;

    @Builder.Default
    String documentType = "SHIPPING_LABEL";

    @Builder.Default
    String documentSize = "A6";

    @Builder.Default
    String documentFormat = "PDF";


    @Override
    public Map<String, String> createParameters() {
        Map<String, String> params = new HashMap<String, String>();

        params.put("shop_cipher", shopCipher);
        params.put("document_type", documentType);
        params.put("document_size", documentSize);
        params.put("document_format", documentFormat);

        return params;
    }
    @Override
    public TiktokApiResponse callApi() throws JsonProcessingException {
        String finalApi = api.replace("{package_id}", packageId);
        return requestClient.get(finalApi, accessToken, createParameters());
    }
}
