package app.allstackproject.privideo.common.redis;

public class RedisKeys {

    private RedisKeys() {
    }

    private static final String ORG_PREFIX = "org:";
    private static final String ORG_CODE_PREFIX = "orgcode:";
    private static final String MEMBER_SUFFIX = ":member:";

    public static String org(Long orgId) {
        return ORG_PREFIX + orgId;
    }

    public static String orgCode(String code) {
        return ORG_CODE_PREFIX + code;
    }

    public static String memberPermission(Long orgId, Long memberId) {
        return ORG_PREFIX + orgId + MEMBER_SUFFIX + memberId;
    }

    public static String orgMembersPattern(Long orgId) {
        return ORG_PREFIX + orgId + MEMBER_SUFFIX + "*";
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
