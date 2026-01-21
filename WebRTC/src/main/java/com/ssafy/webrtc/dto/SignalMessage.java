package com.ssafy.webrtc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalMessage {

    private String type;      // offer, answer, ice-candidate, join, leave
    private String roomId;
    private String senderId;
    private String targetId;
    private Object payload;   // SDP or ICE candidate data
}
