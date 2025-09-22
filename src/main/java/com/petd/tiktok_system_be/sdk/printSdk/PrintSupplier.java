package com.petd.tiktok_system_be.sdk.printSdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.petd.tiktok_system_be.entity.Order.Order;

import java.io.IOException;

public interface PrintSupplier <T> {
    T print () throws IOException;

    String buildBody (Order order) throws JsonProcessingException;
}
