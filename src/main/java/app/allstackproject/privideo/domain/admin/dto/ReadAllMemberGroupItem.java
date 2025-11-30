package app.allstackproject.privideo.domain.admin.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadAllMemberGroupItem {
    private Long id;

    private String name;

    private List<ReadAllCategoryItem> categories;
}
