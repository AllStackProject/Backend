package app.allstackproject.privideo.dto.video;

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
