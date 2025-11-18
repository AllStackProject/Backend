package app.allstackproject.privideo.dto.admin;

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
