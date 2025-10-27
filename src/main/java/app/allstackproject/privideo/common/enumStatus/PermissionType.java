package app.allstackproject.privideo.common.enumStatus;

import lombok.Getter;

@Getter
public enum PermissionType {
    UPLOAD_VIDEO(1L << 0),
    CREATE_GROUP(1L << 1),
    CREATE_HASHTAG(1L << 2);

    private final long bit;

    PermissionType(long bit) {
        this.bit = bit;
    }

    public static long combine(PermissionType... perms) {
        long mask = 0L;
        for (PermissionType p : perms) {
            mask |= p.getBit();
        }
        return mask;
    }

    public static boolean has(long mask, PermissionType p) {
        return (mask & p.getBit()) == p.getBit();
    }
}
