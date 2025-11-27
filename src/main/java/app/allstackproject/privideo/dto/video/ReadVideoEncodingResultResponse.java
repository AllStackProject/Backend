package app.allstackproject.privideo.dto.video;

import app.allstackproject.privideo.common.enumStatus.UploadStatusType;
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
