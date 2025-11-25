package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAllNotificationResponse {
    private List<ReadAllNotificationItem> notifications;

    private ReadAllNotificationResponse(List<ReadAllNotificationItem> notifications) {
        this.notifications = notifications;
    }

    public static ReadAllNotificationResponse of(List<ReadAllNotificationItem> notifications) {
        return new ReadAllNotificationResponse(notifications);
    }
}
