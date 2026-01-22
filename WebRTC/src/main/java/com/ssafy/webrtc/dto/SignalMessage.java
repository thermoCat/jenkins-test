package com.ssafy.webrtc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalMessage {

    private String type;      // offer, answer, ice-candidate, join, leave, room-info
    private String roomId;
    private String senderId;
    private String targetId;
    private Object payload;   // SDP or ICE candidate data
    private List<String> participants;  // List of existing participants (for room-info)
}
