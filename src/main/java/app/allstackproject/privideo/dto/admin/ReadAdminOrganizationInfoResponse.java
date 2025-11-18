package app.allstackproject.privideo.dto.admin;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadAdminOrganizationInfoResponse {
    private String orgName;

    private String imgUrl;

    private Long memberCnt;

    private String orgCode;

    private List<ReadAllMemberGroupItem> memberGroups;

    private ReadAdminOrganizationInfoResponse(String orgName, String imgUrl, Long memberCnt, String orgCode,
                                              List<ReadAllMemberGroupItem> memberGroups) {
        this.orgName = orgName;
        this.imgUrl = imgUrl;
        this.memberCnt = memberCnt;
        this.orgCode = orgCode;
        this.memberGroups = memberGroups;
    }

    public static ReadAdminOrganizationInfoResponse of(String orgName, String imgUrl, Long memberCnt, String orgCode,
                                                       List<ReadAllMemberGroupItem> memberGroups) {
        return new ReadAdminOrganizationInfoResponse(orgName, imgUrl, memberCnt, orgCode, memberGroups);
    }
}
