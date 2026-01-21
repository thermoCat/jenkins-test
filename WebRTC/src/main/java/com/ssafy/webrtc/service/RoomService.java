package com.ssafy.webrtc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RoomService {

    private final Map<String, Set<String>> rooms = new ConcurrentHashMap<>();

    public void joinRoom(String roomId, String sessionId) {
        rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
        log.info("Session {} joined room {}. Room size: {}", sessionId, roomId, rooms.get(roomId).size());
    }

    public void leaveRoom(String roomId, String sessionId) {
        Set<String> room = rooms.get(roomId);
        if (room != null) {
            room.remove(sessionId);
            if (room.isEmpty()) {
                rooms.remove(roomId);
                log.info("Room {} is empty and removed", roomId);
            }
        }
    }

    public Set<String> getRoomParticipants(String roomId) {
        return rooms.getOrDefault(roomId, Collections.emptySet());
    }

    public void removeUserFromAllRooms(String sessionId) {
        rooms.forEach((roomId, participants) -> {
            if (participants.remove(sessionId)) {
                log.info("Session {} removed from room {}", sessionId, roomId);
                if (participants.isEmpty()) {
                    rooms.remove(roomId);
                    log.info("Room {} is empty and removed", roomId);
                }
            }
        });
    }

    public Map<String, Set<String>> getAllRooms() {
        return Collections.unmodifiableMap(rooms);
    }

    public int getRoomCount() {
        return rooms.size();
    }

    public int getParticipantCount(String roomId) {
        Set<String> room = rooms.get(roomId);
        return room != null ? room.size() : 0;
    }
}
