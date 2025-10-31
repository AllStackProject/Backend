package app.allstackproject.privideo.common.redis;

public class RedisKeys {

    private RedisKeys() {
    }

    private static final String ORG_PREFIX = "org:";
    private static final String ORG_CODE_PREFIX = "orgcode:";

    public static String org(Long orgId) {
        return ORG_PREFIX + orgId;
    }

    public static String getOrgCode(String code) {
        return ORG_CODE_PREFIX + code;
    }

    //TODO: enum으로 빼기
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
