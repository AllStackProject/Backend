package app.allstackproject.privideo.domain.video.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoCategoryItem {
    private Long id;

    private String title;

    private Boolean isSelected;
}