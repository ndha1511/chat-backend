package com.project.chatbackend.responses;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageRoomResponse {
    private List<RoomResponse> roomResponses;
    private int totalPage;
}
