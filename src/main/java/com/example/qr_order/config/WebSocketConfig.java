package com.example.qr_order.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt một in-memory message broker đơn giản
        // Nó sẽ gửi message tới các client đã subscribe các đường dẫn có tiền tố là "/topic"
        config.enableSimpleBroker("/topic");
        
        // Tiền tố cho các message được gửi từ client tới server
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // End-point "/ws" là nơi client kết nối vào server qua WebSocket
        // setAllowedOriginPatterns("*") cho phép từ mọi nguồn nếu có cấu hình CORS
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
