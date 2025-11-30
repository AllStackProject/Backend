package app.allstackproject.privideo.domain.organization.util;

import java.security.SecureRandom;

public final class OrgCodeGenerator {
    private static final SecureRandom RND = new SecureRandom();
    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray(); // Base62

    public static String generateCode(long orgId) {
        long nowSec = System.currentTimeMillis() / 1000L;
        long mixed = (orgId * 1315423911L) ^ nowSec ^ (RND.nextInt(1 << 20));

        if (mixed < 0) {
            mixed = -mixed;
        }

        String b62 = toBase62(mixed);
        return leftPadOrTail(b62, 6);
    }

    private static String toBase62(long v) {
        if (v == 0) {
            return "0";
        }

        char[] buf = new char[11];
        int i = buf.length;

        while (v > 0) {
            buf[--i] = ALPHABET[(int) (v % 62)];
            v /= 62;
        }
        return new String(buf, i, buf.length - i);
    }

    private static String leftPadOrTail(String s, int len) {
        if (s.length() == len) {
            return s;
        }

        if (s.length() > len) {
            return s.substring(s.length() - len);
        }

        StringBuilder sb = new StringBuilder(len);
        for (int i = s.length(); i < len; i++) {
            sb.append('0');
        }
        return sb.append(s).toString();
    }
}
