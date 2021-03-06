package net.ldst.chatchik.configurations;

import net.ldst.chatchik.Interceptor.HandShakeHandler;
import net.ldst.chatchik.Interceptor.UserHandshakeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebSockerConfigurer implements WebSocketMessageBrokerConfigurer {

    @Autowired
    HandShakeHandler handShakeHandler;

    @Autowired
    UserHandshakeHandler userHandshakeHandler;

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(2147483647);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/msg");
        registry.setUserDestinationPrefix("/personal");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/message")
                .setHandshakeHandler(userHandshakeHandler)
                .withSockJS()
                .setInterceptors(handShakeHandler);
    }
}
