package app.allstackproject.privideo.global.response;

import lombok.Getter;

@Getter
public class SuccessResponse {
    private final Boolean isSuccess;

    private SuccessResponse(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public static SuccessResponse of(boolean isSuccess) {
        return new SuccessResponse(isSuccess);
    }
}
