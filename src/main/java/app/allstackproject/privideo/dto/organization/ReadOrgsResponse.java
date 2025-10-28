package app.allstackproject.privideo.dto.organization;

import java.util.List;
import lombok.Getter;

@Getter
public class ReadOrgsResponse {
    private List<ReadOrgDto> organizations;

    private ReadOrgsResponse(List<ReadOrgDto> readOrgDtos) {
        this.organizations = readOrgDtos;
    }

    public static ReadOrgsResponse of(List<ReadOrgDto> result) {
        return new ReadOrgsResponse(result);
    }
}
