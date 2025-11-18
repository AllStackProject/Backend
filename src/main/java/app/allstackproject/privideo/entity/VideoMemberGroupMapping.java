package app.allstackproject.privideo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Video_Member_Group_Mapping")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoMemberGroupMapping extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_group_id")
    private MemberGroup memberGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id")
    private Video video;

    @Builder(access = AccessLevel.PRIVATE)
    private VideoMemberGroupMapping(MemberGroup memberGroup, Video video) {
        this.memberGroup = memberGroup;
        this.video = video;
    }

    public static VideoMemberGroupMapping create(MemberGroup memberGroup, Video video) {
        return VideoMemberGroupMapping.builder()
                .memberGroup(memberGroup)
                .video(video)
                .build();
    }
}
