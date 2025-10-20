package com.petd.tiktok_system_be.service.Lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.petd.tiktok_system_be.sdk.SimpleHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class TelegramService {

    @Value("${bot.api-key}")
    String apikey;

    @Value("${bot.chat-id}")
    String defaultChatId;

    SimpleHttpClient httpClient = new SimpleHttpClient(60);
    private final ObjectMapper mapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);;

    public boolean sendMessage(String text, String chatId) {
        String targetChatId = (chatId == null || chatId.isBlank()) ? defaultChatId : chatId;
        return sendMessageToChat(targetChatId, text);
    }

    public boolean sendMessage(String text) {
        return sendMessageToChat(defaultChatId, text);
    }

    public boolean sendMessageToChat(String targetChatId, String text) {
        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=HTML",
                    apikey, targetChatId, encodedText
            );
            httpClient.request(
                    url,
                    "GET",
                    null,
                    ""
            );

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean sendPhoto(String photoUrl, String caption, String chatId) {
        log.info("Sending photo to chat: {}", chatId);
        String targetChatId = (chatId == null || chatId.isBlank()) ? defaultChatId : chatId;
        try {
            String url = String.format(
                    "https://api.telegram.org/bot%s/sendPhoto",apikey);
            BodySendPhoto body = new BodySendPhoto(targetChatId, photoUrl, caption, "HTML");
            String bodyString = mapper.writeValueAsString(body);

            httpClient.request(
                    url,
                    "POST",
                    null,
                    bodyString
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public record BodySendPhoto (
            String chatId,
            String photo,
            String caption,
            String parseMode

    ){}
}
