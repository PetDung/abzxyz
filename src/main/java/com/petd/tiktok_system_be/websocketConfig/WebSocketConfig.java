package com.petd.tiktok_system_be.websocketConfig;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    AuthChannelInterceptor authChannelInterceptor;


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // endpoint FE connect vào
        registry.addEndpoint("/ws-order")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // fallback nếu browser không hỗ trợ websocket
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // client SUBSCRIBE vào prefix này
        registry.enableSimpleBroker("/topic", "/queue");

        // client SEND message với prefix này
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor); // thêm interceptor vừa tạo
    }


}
