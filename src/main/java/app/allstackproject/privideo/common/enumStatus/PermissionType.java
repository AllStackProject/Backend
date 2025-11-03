package app.allstackproject.privideo.common.enumStatus;

import lombok.Getter;

@Getter
public enum PermissionType {
    //    UPLOAD_VIDEO(1L << 0),
//    CREATE_GROUP(1L << 1),
//    CREATE_HASHTAG(1L << 2);
//
//    private final long bit;
//
//    PermissionType(long bit) {
//        this.bit = bit;
//    }
//
//    public static long combine(PermissionType... perms) {
//        long mask = 0L;
//        for (PermissionType p : perms) {
//            mask |= p.getBit();
//        }
//        return mask;
//    }
//
//    public static boolean has(long mask, PermissionType p) {
//        return (mask & p.getBit()) == p.getBit();
//    }

    VIDEO_QUIZ_MANAGE(1L << 0),
    STATS_REPORT(1L << 1),
    NOTICE(1L << 2),
    ORG_SETTING(1L << 3);

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

    public static long add(long mask, PermissionType p) {
        return mask | p.getBit();
    }

    public static long remove(long mask, PermissionType p) {
        return mask & ~p.getBit();
    }

    public static String describe(long mask) {
        StringBuilder sb = new StringBuilder();
        for (PermissionType p : values()) {
            if (has(mask, p)) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(p.name());
            }
        }
        return sb.length() > 0 ? sb.toString() : "NONE";
    }
}
