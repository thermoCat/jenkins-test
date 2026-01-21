package com.ssafy.webrtc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RoomServiceTest {

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService();
    }

    @Test
    @DisplayName("should add user to room when joining")
    void joinRoom_shouldAddUserToRoom() {
        // given
        String roomId = "test-room";
        String sessionId = "session-1";

        // when
        roomService.joinRoom(roomId, sessionId);

        // then
        Set<String> participants = roomService.getRoomParticipants(roomId);
        assertThat(participants).contains(sessionId);
        assertThat(roomService.getParticipantCount(roomId)).isEqualTo(1);
    }

    @Test
    @DisplayName("should allow multiple users to join the same room")
    void joinRoom_shouldAllowMultipleUsers() {
        // given
        String roomId = "test-room";
        String session1 = "session-1";
        String session2 = "session-2";
        String session3 = "session-3";

        // when
        roomService.joinRoom(roomId, session1);
        roomService.joinRoom(roomId, session2);
        roomService.joinRoom(roomId, session3);

        // then
        Set<String> participants = roomService.getRoomParticipants(roomId);
        assertThat(participants).hasSize(3);
        assertThat(participants).containsExactlyInAnyOrder(session1, session2, session3);
    }

    @Test
    @DisplayName("should remove user from room when leaving")
    void leaveRoom_shouldRemoveUserFromRoom() {
        // given
        String roomId = "test-room";
        String session1 = "session-1";
        String session2 = "session-2";
        roomService.joinRoom(roomId, session1);
        roomService.joinRoom(roomId, session2);

        // when
        roomService.leaveRoom(roomId, session1);

        // then
        Set<String> participants = roomService.getRoomParticipants(roomId);
        assertThat(participants).hasSize(1);
        assertThat(participants).contains(session2);
        assertThat(participants).doesNotContain(session1);
    }

    @Test
    @DisplayName("should remove room when last user leaves")
    void leaveRoom_shouldRemoveEmptyRoom() {
        // given
        String roomId = "test-room";
        String sessionId = "session-1";
        roomService.joinRoom(roomId, sessionId);

        // when
        roomService.leaveRoom(roomId, sessionId);

        // then
        assertThat(roomService.getRoomParticipants(roomId)).isEmpty();
        assertThat(roomService.getRoomCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("should remove user from all rooms when disconnecting")
    void removeUserFromAllRooms_shouldRemoveFromAllRooms() {
        // given
        String sessionId = "session-1";
        roomService.joinRoom("room-1", sessionId);
        roomService.joinRoom("room-2", sessionId);
        roomService.joinRoom("room-3", sessionId);

        // when
        roomService.removeUserFromAllRooms(sessionId);

        // then
        assertThat(roomService.getRoomParticipants("room-1")).doesNotContain(sessionId);
        assertThat(roomService.getRoomParticipants("room-2")).doesNotContain(sessionId);
        assertThat(roomService.getRoomParticipants("room-3")).doesNotContain(sessionId);
    }

    @Test
    @DisplayName("should return empty set for non-existent room")
    void getRoomParticipants_shouldReturnEmptySetForNonExistentRoom() {
        // when
        Set<String> participants = roomService.getRoomParticipants("non-existent-room");

        // then
        assertThat(participants).isEmpty();
    }

    @Test
    @DisplayName("should return correct room count")
    void getRoomCount_shouldReturnCorrectCount() {
        // given
        roomService.joinRoom("room-1", "session-1");
        roomService.joinRoom("room-2", "session-2");
        roomService.joinRoom("room-3", "session-3");

        // when & then
        assertThat(roomService.getRoomCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("should return zero for non-existent room participant count")
    void getParticipantCount_shouldReturnZeroForNonExistentRoom() {
        // when
        int count = roomService.getParticipantCount("non-existent-room");

        // then
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("should not add duplicate session to room")
    void joinRoom_shouldNotAddDuplicateSession() {
        // given
        String roomId = "test-room";
        String sessionId = "session-1";

        // when
        roomService.joinRoom(roomId, sessionId);
        roomService.joinRoom(roomId, sessionId);

        // then
        assertThat(roomService.getParticipantCount(roomId)).isEqualTo(1);
    }
}
