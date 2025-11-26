package app.allstackproject.privideo.common.util;

public class RedisUtil {

    private RedisUtil() {
    }

    private static final String ORG_PREFIX = "org:";
    private static final String ORG_CODE_PREFIX = "orgcode:";
    private static final String MEMBER_PREFIX = "member:";
    private static final String WATCH = "watch:";

    public static String getOrgKey(Long orgId) {
        return ORG_PREFIX + orgId;
    }

    public static String getOrgCodeKey(String code) {
        return ORG_CODE_PREFIX + code;
    }

    public static String getMemberPermissionKey(Long orgId, Long memberId) {
        return String.format(ORG_PREFIX + "%d:" + MEMBER_PREFIX + "%d", orgId, memberId);
    }

    public static final class Fields {
        private Fields() {
        }

        public static final String CODE = "code";
        public static final String UPDATED_AT = "updatedAt";
        public static final String ORG_ID = "orgId";
        public static final String CREATED_AT = "createdAt";
        public static final String PERMISSION_CODE = "permissionCode";
    }
}
