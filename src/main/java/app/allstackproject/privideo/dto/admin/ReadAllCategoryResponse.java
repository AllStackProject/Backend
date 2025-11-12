package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAllCategoryResponse {
    private List<ReadAllCategoryDto> categories;

    private ReadAllCategoryResponse(List<ReadAllCategoryDto> categories) {
        this.categories = categories;
    }

    public static ReadAllCategoryResponse of(List<ReadAllCategoryDto> categories) {
        return new ReadAllCategoryResponse(categories);
    }
}
