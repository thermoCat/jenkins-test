package com.ssafy.webrtc;

import com.ssafy.webrtc.handler.SignalingHandler;
import com.ssafy.webrtc.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class WebRtcApplicationTests {

    @Autowired
    private SignalingHandler signalingHandler;

    @Autowired
    private RoomService roomService;

    @Test
    @DisplayName("should load application context successfully")
    void contextLoads() {
        assertThat(signalingHandler).isNotNull();
        assertThat(roomService).isNotNull();
    }
}
