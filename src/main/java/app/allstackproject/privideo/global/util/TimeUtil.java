package app.allstackproject.privideo.global.util;

import java.time.LocalDateTime;


public class TimeUtil {
    public static LocalDateTime calculateStartDate(int recentMonths) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime firstDayOfCurrentMonth = now
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        return firstDayOfCurrentMonth.minusMonths(recentMonths - 1);
    }
}
