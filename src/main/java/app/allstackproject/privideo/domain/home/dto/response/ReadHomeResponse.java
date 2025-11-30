package app.allstackproject.privideo.domain.home.dto.response;

import app.allstackproject.privideo.dto.home.HomeVideoItem;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadHomeResponse {
    private String nickname;

    private Boolean isAdmin;

    private String orgName;

    private List<HomeVideoItem> videos;

    private List<String> categories;

    private ReadHomeResponse(String nickname, Boolean isAdmin, String orgName, List<HomeVideoItem> videos,
                             List<String> categories) {
        this.nickname = nickname;
        this.isAdmin = isAdmin;
        this.orgName = orgName;
        this.videos = videos;
        this.categories = categories;
    }

    public static ReadHomeResponse of(String nickname, Boolean isAdmin, String orgName, List<HomeVideoItem> videos,
                                      List<String> categories) {
        return new ReadHomeResponse(nickname, isAdmin, orgName, videos, categories);
    }
}
