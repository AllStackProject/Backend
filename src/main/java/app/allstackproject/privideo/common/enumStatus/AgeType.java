package app.allstackproject.privideo.common.enumStatus;

import lombok.Getter;

@Getter
public enum AgeType {
    TEN(10),
    TWENTY(20),
    THIRTY(30),
    FORTY(40),
    FIFTY(50),
    SIXTY(60);

    private final int value;

    AgeType(int value) {
        this.value = value;
    }
}
