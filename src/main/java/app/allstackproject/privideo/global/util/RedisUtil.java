package app.allstackproject.privideo.global.util;

public class RedisUtil {

    private RedisUtil() {
    }

    private static final String ORG_PREFIX = "org:";
    private static final String ORG_CODE_PREFIX = "orgCode:";
    private static final String MEMBER_PREFIX = "member:";
    private static final String WATCH_PREFIX = "watch:";

    public static String getOrgKey(Long orgId) {
        return ORG_PREFIX + orgId;
    }

    public static String getOrgCodeKey(String code) {
        return ORG_CODE_PREFIX + code;
    }

    public static String getMemberPermissionKey(Long orgId, Long memberId) {
        return String.format(ORG_PREFIX + "%d:" + MEMBER_PREFIX + "%d", orgId, memberId);
    }

    public static String getWatchSessionKey(String sessionId) {
        return WATCH_PREFIX + sessionId;
    }

    public static final class Fields {
        private Fields() {
        }

        public static final String CODE = "orgCode";

        public static final String PERMISSION_CODE = "permissionCode";

        public static final String MEMBER_ID = "memberId";
    }
}
