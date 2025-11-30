package app.allstackproject.privideo.domain.organization.dto.enums;

import lombok.Getter;

@Getter
public enum PermissionType {
    VIDEO_MANAGE(1L << 0),
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
}
