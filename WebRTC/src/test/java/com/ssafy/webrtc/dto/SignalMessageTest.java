package com.ssafy.webrtc.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignalMessageTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("should serialize to JSON correctly")
    void shouldSerializeToJson() throws Exception {
        // given
        SignalMessage message = SignalMessage.builder()
                .type("offer")
                .roomId("test-room")
                .senderId("sender-123")
                .targetId("target-456")
                .payload("{\"sdp\": \"test\"}")
                .build();

        // when
        String json = objectMapper.writeValueAsString(message);

        // then
        assertThat(json).contains("\"type\":\"offer\"");
        assertThat(json).contains("\"roomId\":\"test-room\"");
        assertThat(json).contains("\"senderId\":\"sender-123\"");
        assertThat(json).contains("\"targetId\":\"target-456\"");
    }

    @Test
    @DisplayName("should deserialize from JSON correctly")
    void shouldDeserializeFromJson() throws Exception {
        // given
        String json = """
                {
                    "type": "answer",
                    "roomId": "test-room",
                    "senderId": "sender-123",
                    "targetId": "target-456",
                    "payload": {"sdp": "test-sdp"}
                }
                """;

        // when
        SignalMessage message = objectMapper.readValue(json, SignalMessage.class);

        // then
        assertThat(message.getType()).isEqualTo("answer");
        assertThat(message.getRoomId()).isEqualTo("test-room");
        assertThat(message.getSenderId()).isEqualTo("sender-123");
        assertThat(message.getTargetId()).isEqualTo("target-456");
        assertThat(message.getPayload()).isNotNull();
    }

    @Test
    @DisplayName("should create message using builder pattern")
    void shouldCreateUsingBuilder() {
        // when
        SignalMessage message = SignalMessage.builder()
                .type("ice-candidate")
                .roomId("room-1")
                .build();

        // then
        assertThat(message.getType()).isEqualTo("ice-candidate");
        assertThat(message.getRoomId()).isEqualTo("room-1");
        assertThat(message.getSenderId()).isNull();
        assertThat(message.getTargetId()).isNull();
    }

    @Test
    @DisplayName("should set and get all fields")
    void shouldSetAndGetAllFields() {
        // given
        SignalMessage message = new SignalMessage();

        // when
        message.setType("join");
        message.setRoomId("room-123");
        message.setSenderId("user-1");
        message.setTargetId("user-2");
        message.setPayload("test-payload");

        // then
        assertThat(message.getType()).isEqualTo("join");
        assertThat(message.getRoomId()).isEqualTo("room-123");
        assertThat(message.getSenderId()).isEqualTo("user-1");
        assertThat(message.getTargetId()).isEqualTo("user-2");
        assertThat(message.getPayload()).isEqualTo("test-payload");
    }

    @Test
    @DisplayName("should handle complex payload objects")
    void shouldHandleComplexPayload() throws Exception {
        // given
        String jsonWithComplexPayload = """
                {
                    "type": "offer",
                    "roomId": "test-room",
                    "payload": {
                        "type": "offer",
                        "sdp": "v=0\\r\\no=- 123456 2 IN IP4 127.0.0.1\\r\\n"
                    }
                }
                """;

        // when
        SignalMessage message = objectMapper.readValue(jsonWithComplexPayload, SignalMessage.class);

        // then
        assertThat(message.getPayload()).isNotNull();
    }
}
