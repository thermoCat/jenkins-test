package com.ssafy.webrtc.controller;

import com.ssafy.webrtc.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    @Test
    @DisplayName("should return empty list when no rooms exist")
    void getRooms_shouldReturnEmptyList() {
        // given
        when(roomService.getAllRooms()).thenReturn(Map.of());

        // when
        ResponseEntity<List<RoomController.RoomInfo>> response = roomController.getRooms();

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("should return room list with participant counts")
    void getRooms_shouldReturnRoomList() {
        // given
        Map<String, Set<String>> rooms = new HashMap<>();
        rooms.put("room-1", Set.of("user-1", "user-2"));
        rooms.put("room-2", Set.of("user-3"));
        when(roomService.getAllRooms()).thenReturn(rooms);

        // when
        ResponseEntity<List<RoomController.RoomInfo>> response = roomController.getRooms();

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);

        List<RoomController.RoomInfo> roomInfos = response.getBody();
        assertThat(roomInfos).extracting("roomId").containsExactlyInAnyOrder("room-1", "room-2");
    }

    @Test
    @DisplayName("should return correct room count")
    void getRoomCount_shouldReturnCount() {
        // given
        when(roomService.getRoomCount()).thenReturn(5);

        // when
        ResponseEntity<Integer> response = roomController.getRoomCount();

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(5);
    }
}
