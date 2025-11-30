package app.allstackproject.privideo.domain.video.dto.response;

import app.allstackproject.privideo.domain.video.enums.UploadStatusType;
import lombok.Getter;

@Getter
public class ReadVideoEncodingResultResponse {
    private UploadStatusType uploadStatus;

    private ReadVideoEncodingResultResponse(UploadStatusType uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public static ReadVideoEncodingResultResponse of(UploadStatusType uploadStatus) {
        return new ReadVideoEncodingResultResponse(uploadStatus);
    }
}
