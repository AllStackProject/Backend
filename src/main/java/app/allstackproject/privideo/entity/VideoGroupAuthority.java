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
@Table(name = "Video_Group_Authority")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoGroupAuthority extends BaseEntity {
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
    private VideoGroupAuthority(MemberGroup memberGroup, Video video) {
        this.memberGroup = memberGroup;
        this.video = video;
    }

    public static VideoGroupAuthority create(MemberGroup memberGroup, Video video) {
        return VideoGroupAuthority.builder()
                .memberGroup(memberGroup)
                .video(video)
                .build();
    }
}
