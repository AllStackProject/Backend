package app.allstackproject.privideo.domain.video.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoMemberGroupItem {
    private Long id;

    private String name;

    private Boolean isSelected;

    private List<VideoCategoryItem> categories;
}
