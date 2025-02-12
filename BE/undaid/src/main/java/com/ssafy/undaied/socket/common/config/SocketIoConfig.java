package com.ssafy.undaied.socket.common.config;

import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.corundumstudio.socketio.SocketIOServer;

/**
 * SocketIoConfig.
 */
@Configuration
public class SocketIoConfig {

    @Value("${socketio.server.hostname}")
    private String hostname;

    @Value("${socketio.server.port}")
    private int port;

    @Bean
    public SocketIONamespace socketIoNamespace(SocketIOServer server) {
        return server.getNamespace("/socket.io");
    }

    /**
     * Tomcat 서버와 별도로 돌아가는 netty 서버를 생성
     */
    @Bean
    public SocketIOServer socketIoServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(hostname);
        config.setPort(port);
        config.setOrigin("*");
        config.setJsonSupport(new JacksonJsonSupport(new JavaTimeModule()));

        // 연결 관련 타임아웃 설정
        config.setUpgradeTimeout(10000);
        config.setPingTimeout(60000);
        config.setPingInterval(25000);
        config.setAllowCustomRequests(true);
        config.setRandomSession(false);

        // LocalDateTime 직렬화 및 역직렬화 위한 설정
        JacksonJsonSupport jsonSupport = new JacksonJsonSupport(new JavaTimeModule());
        config.setJsonSupport(jsonSupport);

        SocketIOServer server = new SocketIOServer(config);
        server.removeAllListeners("error"); // 기본 에러 리스너 제거

        // 기본 네임스페이스 연결 리스너
        server.addNamespace("/socket.io");

        return server;
    }
}
