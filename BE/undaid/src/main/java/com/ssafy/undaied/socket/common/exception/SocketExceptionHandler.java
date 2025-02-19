package com.ssafy.undaied.socket.common.exception;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ExceptionListenerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocketExceptionHandler extends ExceptionListenerAdapter {

    private final SocketIOServer socketIOServer;

    @Override
    public void onEventException(Exception e, List<Object> args, SocketIOClient client) {
        if (e instanceof SocketException socketException) {
            handleError(client, socketException.getErrorCode());
        } else {
            handleError(client, SocketErrorCode.SOCKET_EVENT_ERROR);
        }
    }

    @Override
    public void onDisconnectException(Exception e, SocketIOClient client) {
        handleError(client, SocketErrorCode.SOCKET_DISCONNECTED);
    }

    @Override
    public void onConnectException(Exception e, SocketIOClient client) {
        handleError(client, SocketErrorCode.SOCKET_CONNECTION_FAILED);
    }

    @Override
    public void onAuthException(Throwable throwable, SocketIOClient client) {
        handleError(client, SocketErrorCode.SOCKET_AUTHENTICATION_FAILED);
    }

    @Override
    public void onPingException(Exception e, SocketIOClient client) {
        log.error("Ping error: {}", e.getMessage());
    }

    private void handleError(SocketIOClient client, SocketErrorCode errorCode) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("code", errorCode.getStatus());
        errorData.put("message", errorCode.getMessage());

        log.error("Socket error occurred: {} - {}", errorCode.getStatus(), errorCode.getMessage());
        client.sendEvent("error", errorData);
    }
}