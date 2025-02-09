// package com.ssafy.undaied.socket.common.config;

// import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// import com.corundumstudio.socketio.SocketIOServer;

// import com.corundumstudio.socketio.Transport; // 추가사항

// /**
//  * SocketIoConfig.
//  */
// @Configuration
// public class SocketIoConfig {

//     @Value("${socketio.server.hostname}")
//     private String hostname;

//     @Value("${socketio.server.port}")
//     private int port;

//     /**
//      * Tomcat 서버와 별도로 돌아가는 netty 서버를 생성
//      */
//     @Bean
//     public SocketIOServer socketIoServer() {
//         com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
//         config.setHostname(hostname);
//         config.setPort(port);
//         config.setOrigin("*");

//         // 추가 수정 사항 시작
//         config.setUpgradeTimeout(10000);
//         config.setPingTimeout(60000);
//         config.setPingInterval(25000);
//         config.setFirstDataTimeout(180000);
//         config.setAllowCustomRequests(true);  // custom headers 허용

//         // transport 설정
//         config.setTransports(Transport.WEBSOCKET, Transport.POLLING);
//         // 추가 수정 사항 종료

//         //LocalDateTime 직렬화 및 역직렬화 위한 설정
//         config.setJsonSupport(new JacksonJsonSupport(new JavaTimeModule()));

//         // 디버그 모드 활성화
//         config.setRandomSession(false); //
//         SocketIOServer server = new SocketIOServer(config);
//         server.removeAllListeners("error");  // 기본 에러 리스너 제거

//         // 추가적인 설정 재시작
//         server.addConnectListener(client -> {
//             System.out.println("Client connected: " + client.getHandshakeData().getHttpHeaders());
//             // JWT 토큰 확인
//             String token = client.getHandshakeData().getHttpHeaders().get("Authorization");
//             System.out.println("Auth token: " + token);
//         });
//         // 추가적인 설정 종료

//         return server;
//     }
// }

package com.ssafy.undaied.socket.common.config;

import com.corundumstudio.socketio.AckMode;
import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;
import lombok.extern.slf4j.Slf4j;

/**
 * SocketIoConfig.
 */
@Configuration
@Slf4j
public class SocketIoConfig {

    @Value("${socketio.server.hostname}")
    private String hostname;

    @Value("${socketio.server.port}")
    private int port;

    /**
     * Tomcat 서버와 별도로 돌아가는 netty 서버를 생성
     */
    @Bean
    public SocketIOServer socketIoServer() {
        System.setProperty("io.netty.tryReflectionSafefail", "false"); //
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(9090);
        // config.setHostname(hostname);
        // config.setPort(port);

        // CORS 설정 변경
        // config.setOrigin("https://i12b212.p.ssafy.io"); // null 대신 실제 도메인
        config.setOrigin(null); // 모든 origin 허용

        // 웹소켓 전용 설정
        config.setTransports(Transport.WEBSOCKET);

        // 프로토콜 관련
        config.setPingTimeout(60000); // 60초
        config.setPingInterval(25000); // 25초
        config.setFirstDataTimeout(5000); // 5초

        // 연결 설정
        config.setAllowCustomRequests(true);
        config.setUpgradeTimeout(10000);
        config.setMaxHttpContentLength(1024 * 1024);
        config.setMaxFramePayloadLength(1024 * 1024);

        // Socket.IO 4.x 버전 호환성을 위한 설정
        config.setRandomSession(false);

        // LocalDateTime 직렬화 및 역직렬화 위한 설정
        config.setJsonSupport(new JacksonJsonSupport(new JavaTimeModule()));

        SocketIOServer server = new SocketIOServer(config);

        // 에러 핸들링을 위한 리스너 설정
        server.removeAllListeners("error");


        server.addEventListener("error", Object.class, (client, data, ackSender) -> {
            log.error("Socket.IO Error - Client: {}, Data: {}", client.getSessionId(), data);
        });
        // 연결 디버깅을 위한 리스너
        server.addConnectListener(client -> {
            log.info("Client connected: {}", client.getHandshakeData().getHttpHeaders());
            String token = client.getHandshakeData().getHttpHeaders().get("Authorization");
            log.info("Auth token: {}", token);
        });

        server.addEventListener("disconnect", Object.class, (client, data, ackRequest) -> {
            log.debug("Client {} disconnected, last transport was {}",
                    client.getSessionId(),
                    client.getTransport().getValue());
        });

        server.addDisconnectListener(client -> {
            log.debug("Client {} disconnected with transport {}",
                    client.getSessionId(),
                    client.getTransport().getValue());
        });



        return server;
    }
}