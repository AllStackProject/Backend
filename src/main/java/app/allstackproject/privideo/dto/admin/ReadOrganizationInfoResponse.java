package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadOrganizationInfoResponse {
    private String orgName;

    private String imgUrl;

    private Long memberCnt;

    private String orgCode;

    private List<ReadAllMemberGroupItem> memberGroups;

    private ReadOrganizationInfoResponse(String orgName, String imgUrl, Long memberCnt, String orgCode,
                                         List<ReadAllMemberGroupItem> memberGroups) {
        this.orgName = orgName;
        this.imgUrl = imgUrl;
        this.memberCnt = memberCnt;
        this.orgCode = orgCode;
        this.memberGroups = memberGroups;
    }

    public static ReadOrganizationInfoResponse of(String orgName, String imgUrl, Long memberCnt, String orgCode,
                                                  List<ReadAllMemberGroupItem> memberGroups) {
        return new ReadOrganizationInfoResponse(orgName, imgUrl, memberCnt, orgCode, memberGroups);
    }
}
