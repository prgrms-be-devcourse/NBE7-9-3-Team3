package org.example.backend.global.websocketconfig;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    /* 메세지 수신/발신 경로 지정
        - 채팅방 번호별로 구독 주소를 달리 해서 충돌 방지
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/receive");  // 서버 -> 클라이언트
        registry.setApplicationDestinationPrefixes("/send");         // 클라이언트 -> 서버
    }

    // 클라이언트가 처음 연결 시도 시 접속할 주소 설정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // -> ws://localhost:8080/ws
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /*
    STOMP 인증/인가 등록
        - 클라이언트에서 들어오는 메세지를 stompHandler가 인터셉트함
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}