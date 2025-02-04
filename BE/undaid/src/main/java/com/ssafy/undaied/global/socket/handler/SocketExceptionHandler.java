package com.ssafy.undaied.global.socket.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.ssafy.undaied.global.common.exception.BaseException;
import com.ssafy.undaied.global.common.response.ApiResponse;
import org.springframework.stereotype.Component;

import static com.ssafy.undaied.global.common.exception.ErrorCode.SERVER_ERROR;

@Component
public class SocketExceptionHandler {

    public void handleSocketException(SocketIOClient client, BaseException e) {
        ApiResponse errorResponse = ApiResponse.of(e.getErrorCode());
        // 클라이언트에게 에러 응답 전송
        client.sendEvent("error", errorResponse);
        System.out.println(e.getMessage());
        client.disconnect();
    }

    // 일반 Exception을 BaseException으로 변환하여 처리하는 메서드
    public void handleSocketException(SocketIOClient client, Exception e) {
        handleSocketException(client, new BaseException(SERVER_ERROR));
    }
}