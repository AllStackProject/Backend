package app.allstackproject.privideo.domain.video.entity;

import app.allstackproject.privideo.shared.entity.BaseEntity;
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
@Table(name = "Video_Category_Mapping")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoCategoryMapping extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Builder(access = AccessLevel.PRIVATE)
    private VideoCategoryMapping(Video video, Category category) {
        this.video = video;
        this.category = category;
    }

    public static VideoCategoryMapping create(Video video, Category category) {
        return VideoCategoryMapping.builder()
                .video(video)
                .category(category)
                .build();
    }
}
