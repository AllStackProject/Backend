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
@Table(name = "Video_Hashtag_Mapping")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoHashtagMapping extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hashtag_id")
    private Hashtag hashtag;

    @Builder(access = AccessLevel.PRIVATE)
    private VideoHashtagMapping(Video video, Hashtag hashtag) {
        this.video = video;
        this.hashtag = hashtag;
    }

    public static VideoHashtagMapping create(Video video, Hashtag hashtag) {
        return VideoHashtagMapping.builder()
                .video(video)
                .hashtag(hashtag)
                .build();
    }
}
