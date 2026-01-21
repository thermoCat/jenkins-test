package com.ssafy.webrtc.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.webrtc.dto.SignalMessage;
import com.ssafy.webrtc.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignalingHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final RoomService roomService;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("WebSocket connected: {}", sessionId);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received message: {}", payload);

        SignalMessage signalMessage = objectMapper.readValue(payload, SignalMessage.class);
        signalMessage.setSenderId(session.getId());

        switch (signalMessage.getType()) {
            case "join" -> handleJoin(session, signalMessage);
            case "offer" -> handleOffer(signalMessage);
            case "answer" -> handleAnswer(signalMessage);
            case "ice-candidate" -> handleIceCandidate(signalMessage);
            case "leave" -> handleLeave(session, signalMessage);
            default -> log.warn("Unknown message type: {}", signalMessage.getType());
        }
    }

    private void handleJoin(WebSocketSession session, SignalMessage message) throws IOException {
        String roomId = message.getRoomId();
        String sessionId = session.getId();

        roomService.joinRoom(roomId, sessionId);

        // Notify existing users in the room
        for (String participantId : roomService.getRoomParticipants(roomId)) {
            if (!participantId.equals(sessionId)) {
                SignalMessage notification = SignalMessage.builder()
                        .type("user-joined")
                        .roomId(roomId)
                        .senderId(sessionId)
                        .build();
                sendMessage(participantId, notification);
            }
        }

        log.info("User {} joined room {}", sessionId, roomId);
    }

    private void handleOffer(SignalMessage message) throws IOException {
        sendMessage(message.getTargetId(), message);
        log.debug("Offer sent from {} to {}", message.getSenderId(), message.getTargetId());
    }

    private void handleAnswer(SignalMessage message) throws IOException {
        sendMessage(message.getTargetId(), message);
        log.debug("Answer sent from {} to {}", message.getSenderId(), message.getTargetId());
    }

    private void handleIceCandidate(SignalMessage message) throws IOException {
        sendMessage(message.getTargetId(), message);
        log.debug("ICE candidate sent from {} to {}", message.getSenderId(), message.getTargetId());
    }

    private void handleLeave(WebSocketSession session, SignalMessage message) throws IOException {
        String roomId = message.getRoomId();
        String sessionId = session.getId();

        roomService.leaveRoom(roomId, sessionId);

        // Notify remaining users
        for (String participantId : roomService.getRoomParticipants(roomId)) {
            SignalMessage notification = SignalMessage.builder()
                    .type("user-left")
                    .roomId(roomId)
                    .senderId(sessionId)
                    .build();
            sendMessage(participantId, notification);
        }

        log.info("User {} left room {}", sessionId, roomId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        roomService.removeUserFromAllRooms(sessionId);
        log.info("WebSocket disconnected: {}", sessionId);
    }

    private void sendMessage(String sessionId, SignalMessage message) throws IOException {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }

    public Map<String, WebSocketSession> getSessions() {
        return sessions;
    }
}
