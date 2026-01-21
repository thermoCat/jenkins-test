package com.ssafy.webrtc.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.webrtc.dto.SignalMessage;
import com.ssafy.webrtc.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignalingHandlerTest {

    private SignalingHandler signalingHandler;
    private ObjectMapper objectMapper;

    @Mock
    private RoomService roomService;

    @Mock
    private WebSocketSession session1;

    @Mock
    private WebSocketSession session2;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        signalingHandler = new SignalingHandler(objectMapper, roomService);
    }

    @Test
    @DisplayName("should store session when connection is established")
    void afterConnectionEstablished_shouldStoreSession() {
        // given
        when(session1.getId()).thenReturn("session-1");

        // when
        signalingHandler.afterConnectionEstablished(session1);

        // then
        assertThat(signalingHandler.getSessions()).containsKey("session-1");
    }

    @Test
    @DisplayName("should remove session and cleanup when connection is closed")
    void afterConnectionClosed_shouldRemoveSessionAndCleanup() {
        // given
        when(session1.getId()).thenReturn("session-1");
        signalingHandler.afterConnectionEstablished(session1);

        // when
        signalingHandler.afterConnectionClosed(session1, CloseStatus.NORMAL);

        // then
        assertThat(signalingHandler.getSessions()).doesNotContainKey("session-1");
        verify(roomService).removeUserFromAllRooms("session-1");
    }

    @Test
    @DisplayName("should handle join message and notify existing users")
    void handleTextMessage_joinMessage_shouldJoinRoomAndNotify() throws Exception {
        // given
        when(session1.getId()).thenReturn("session-1");
        when(session2.getId()).thenReturn("session-2");
        when(session2.isOpen()).thenReturn(true);

        signalingHandler.afterConnectionEstablished(session1);
        signalingHandler.afterConnectionEstablished(session2);

        when(roomService.getRoomParticipants("test-room"))
                .thenReturn(Set.of("session-1", "session-2"));

        SignalMessage joinMessage = SignalMessage.builder()
                .type("join")
                .roomId("test-room")
                .build();
        String json = objectMapper.writeValueAsString(joinMessage);

        // when
        signalingHandler.handleTextMessage(session1, new TextMessage(json));

        // then
        verify(roomService).joinRoom("test-room", "session-1");
        verify(session2).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("should forward offer message to target user")
    void handleTextMessage_offerMessage_shouldForwardToTarget() throws Exception {
        // given
        when(session1.getId()).thenReturn("session-1");
        when(session2.getId()).thenReturn("session-2");
        when(session2.isOpen()).thenReturn(true);

        signalingHandler.afterConnectionEstablished(session1);
        signalingHandler.afterConnectionEstablished(session2);

        SignalMessage offerMessage = SignalMessage.builder()
                .type("offer")
                .roomId("test-room")
                .targetId("session-2")
                .payload("{\"sdp\": \"test-sdp\", \"type\": \"offer\"}")
                .build();
        String json = objectMapper.writeValueAsString(offerMessage);

        // when
        signalingHandler.handleTextMessage(session1, new TextMessage(json));

        // then
        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session2).sendMessage(messageCaptor.capture());

        SignalMessage sentMessage = objectMapper.readValue(
                messageCaptor.getValue().getPayload(), SignalMessage.class);
        assertThat(sentMessage.getType()).isEqualTo("offer");
        assertThat(sentMessage.getSenderId()).isEqualTo("session-1");
    }

    @Test
    @DisplayName("should forward answer message to target user")
    void handleTextMessage_answerMessage_shouldForwardToTarget() throws Exception {
        // given
        when(session1.getId()).thenReturn("session-1");
        when(session2.getId()).thenReturn("session-2");
        when(session2.isOpen()).thenReturn(true);

        signalingHandler.afterConnectionEstablished(session1);
        signalingHandler.afterConnectionEstablished(session2);

        SignalMessage answerMessage = SignalMessage.builder()
                .type("answer")
                .roomId("test-room")
                .targetId("session-2")
                .payload("{\"sdp\": \"test-sdp\", \"type\": \"answer\"}")
                .build();
        String json = objectMapper.writeValueAsString(answerMessage);

        // when
        signalingHandler.handleTextMessage(session1, new TextMessage(json));

        // then
        verify(session2).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("should forward ICE candidate message to target user")
    void handleTextMessage_iceCandidateMessage_shouldForwardToTarget() throws Exception {
        // given
        when(session1.getId()).thenReturn("session-1");
        when(session2.getId()).thenReturn("session-2");
        when(session2.isOpen()).thenReturn(true);

        signalingHandler.afterConnectionEstablished(session1);
        signalingHandler.afterConnectionEstablished(session2);

        SignalMessage iceMessage = SignalMessage.builder()
                .type("ice-candidate")
                .roomId("test-room")
                .targetId("session-2")
                .payload("{\"candidate\": \"test-candidate\"}")
                .build();
        String json = objectMapper.writeValueAsString(iceMessage);

        // when
        signalingHandler.handleTextMessage(session1, new TextMessage(json));

        // then
        verify(session2).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("should handle leave message and notify remaining users")
    void handleTextMessage_leaveMessage_shouldLeaveRoomAndNotify() throws Exception {
        // given
        when(session1.getId()).thenReturn("session-1");
        when(session2.getId()).thenReturn("session-2");
        when(session2.isOpen()).thenReturn(true);

        signalingHandler.afterConnectionEstablished(session1);
        signalingHandler.afterConnectionEstablished(session2);

        when(roomService.getRoomParticipants("test-room"))
                .thenReturn(Set.of("session-2"));

        SignalMessage leaveMessage = SignalMessage.builder()
                .type("leave")
                .roomId("test-room")
                .build();
        String json = objectMapper.writeValueAsString(leaveMessage);

        // when
        signalingHandler.handleTextMessage(session1, new TextMessage(json));

        // then
        verify(roomService).leaveRoom("test-room", "session-1");
        verify(session2).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("should not send message to closed session")
    void handleTextMessage_closedSession_shouldNotSendMessage() throws Exception {
        // given
        when(session1.getId()).thenReturn("session-1");
        when(session2.getId()).thenReturn("session-2");
        when(session2.isOpen()).thenReturn(false);

        signalingHandler.afterConnectionEstablished(session1);
        signalingHandler.afterConnectionEstablished(session2);

        SignalMessage offerMessage = SignalMessage.builder()
                .type("offer")
                .roomId("test-room")
                .targetId("session-2")
                .payload("{\"sdp\": \"test-sdp\"}")
                .build();
        String json = objectMapper.writeValueAsString(offerMessage);

        // when
        signalingHandler.handleTextMessage(session1, new TextMessage(json));

        // then
        verify(session2, never()).sendMessage(any(TextMessage.class));
    }
}
