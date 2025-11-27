package app.allstackproject.privideo.dto.video;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LeaveVideoSessionInfo {
    private final Long orgId;

    private final Long videoId;

    private final String sessionId;

    private final Long watchRate;

    private final String watchSegments;

    private final Long recentPosition;

    private final Boolean isQuit;

    @Builder(access = AccessLevel.PRIVATE)
    private LeaveVideoSessionInfo(Long orgId, Long videoId, String sessionId, Long watchRate,
                                  String watchSegments, Long recentPosition, Boolean isQuit) {
        this.orgId = orgId;
        this.videoId = videoId;
        this.sessionId = sessionId;
        this.watchRate = watchRate;
        this.watchSegments = watchSegments;
        this.recentPosition = recentPosition;
        this.isQuit = isQuit;
    }

    public static LeaveVideoSessionInfo create(Long orgId, Long videoId,
                                               LeaveVideoSessionRequest leaveVideoSessionRequest) {
        return LeaveVideoSessionInfo.builder()
                .orgId(orgId)
                .videoId(videoId)
                .sessionId(leaveVideoSessionRequest.getSessionId())
                .watchRate(leaveVideoSessionRequest.getWatchRate())
                .watchSegments(leaveVideoSessionRequest.getWatchSegments())
                .recentPosition(leaveVideoSessionRequest.getRecentPosition())
                .isQuit(leaveVideoSessionRequest.getIsQuit())
                .build();
    }
}
