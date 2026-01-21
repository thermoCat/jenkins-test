package com.ssafy.webrtc.controller;

import com.ssafy.webrtc.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<List<RoomInfo>> getRooms() {
        Map<String, Set<String>> allRooms = roomService.getAllRooms();

        List<RoomInfo> rooms = allRooms.entrySet().stream()
                .map(entry -> new RoomInfo(entry.getKey(), entry.getValue().size()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getRoomCount() {
        return ResponseEntity.ok(roomService.getRoomCount());
    }

    @GetMapping("/{roomId}/exists")
    public ResponseEntity<RoomExistsResponse> checkRoomExists(@PathVariable String roomId) {
        int participantCount = roomService.getParticipantCount(roomId);
        boolean exists = participantCount > 0;
        return ResponseEntity.ok(new RoomExistsResponse(exists, participantCount));
    }

    public record RoomInfo(String roomId, int participantCount) {}
    public record RoomExistsResponse(boolean exists, int participantCount) {}
}
