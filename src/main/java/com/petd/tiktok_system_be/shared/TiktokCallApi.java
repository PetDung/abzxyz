package com.petd.tiktok_system_be.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.petd.tiktok_system_be.sdk.TiktokApiResponse;
import com.petd.tiktok_system_be.sdk.exception.TiktokException;

import java.util.Map;

public interface TiktokCallApi {
  Map<String, String> createParameters ();
  TiktokApiResponse callApi()  throws JsonProcessingException ;
}
