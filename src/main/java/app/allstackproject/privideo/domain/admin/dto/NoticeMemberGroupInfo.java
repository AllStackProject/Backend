package app.allstackproject.privideo.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NoticeMemberGroupInfo {
    private Long id;

    private String name;

    private Boolean isSelected;
}
