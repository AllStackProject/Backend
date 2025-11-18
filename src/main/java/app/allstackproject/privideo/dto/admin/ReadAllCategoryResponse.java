package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAllCategoryResponse {
    private List<ReadAllCategoryItem> categories;

    private ReadAllCategoryResponse(List<ReadAllCategoryItem> categories) {
        this.categories = categories;
    }

    public static ReadAllCategoryResponse of(List<ReadAllCategoryItem> categories) {
        return new ReadAllCategoryResponse(categories);
    }
}
