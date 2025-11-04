package app.allstackproject.privideo.common.util;

import java.math.BigInteger;

public class WatchSegmentsUtil {

    public static int countWatchedSegments(BigInteger mask) {
        return mask.bitCount();
    }

    public static boolean watchedLastSegment(BigInteger mask) {
        return mask.testBit(0); // LSB
    }
}
