package app.allstackproject.privideo.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyWatchItem {
    private int year;
    
    private int month;

    private Long watchedVideoCnt;
}
