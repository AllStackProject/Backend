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
@Table(name = "Scrap")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scrap extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id")
    private Video video;

    @Builder(access = AccessLevel.PRIVATE)
    private Scrap(Member member, Video video) {
        this.member = member;
        this.video = video;
    }

    public static Scrap create(Member member, Video video) {
        return Scrap.builder()
                .member(member)
                .video(video)
                .build();
    }
}
