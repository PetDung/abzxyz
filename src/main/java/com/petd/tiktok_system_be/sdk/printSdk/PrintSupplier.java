package com.petd.tiktok_system_be.sdk.printSdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.petd.tiktok_system_be.entity.Order.Order;
import com.petd.tiktok_system_be.sdk.printSdk.PrinteesHub.dto.response.OrderResponse;

import java.io.IOException;

public interface PrintSupplier {
    OrderResponse print (Order order) throws IOException;
    String getCode();

    OrderResponse cancel(Order order) throws IOException;

    OrderResponse synchronize(Order order) throws IOException;

    JsonNode getPrintOrderById (String orderId) throws IOException;
}
