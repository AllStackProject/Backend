package app.allstackproject.privideo.domain.video.entity;

import static app.allstackproject.privideo.domain.video.enums.UploadStatusType.IN_PROGRESS;

import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.organization.entity.Organization;
import app.allstackproject.privideo.shared.entity.BaseEntity;
import app.allstackproject.privideo.domain.video.enums.AiFunctionType;
import app.allstackproject.privideo.domain.video.enums.UploadStatusType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "Video")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member creator;

    @NotBlank
    private String title;

    @NotBlank
    @Column(columnDefinition = "text")
    private String description;

    @NotNull
    @Column(name = "video_url")
    private String videoKey;

    @NotNull
    @Column(name = "thumbnail_url")
    private String thumbnailKey;

    @Setter
    @NotNull
    private String hlsPrefix;

    @NotNull
    private Long wholeTime;

    @NotNull
    private Boolean isComment;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_function_type")
    private AiFunctionType aiFunctionType;

    @Setter
    @Column(columnDefinition = "text")
    private String aiFeedback;

    @Setter
    @Column(columnDefinition = "text")
    private String aiSummary;

    @NotNull
    private LocalDate expiredAt;

    @NotNull
    private Long watchCnt;

    @NotNull
    private Long quitCnt;

    @Setter
    @NotNull
    @Enumerated(EnumType.STRING)
    private UploadStatusType uploadStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private Video(Organization organization, Member creator, String title, String description, String videoKey,
                  String thumbnailKey, String hlsPrefix, Long wholeTime, boolean isComment,
                  AiFunctionType aiFunctionType, String aiFeedback, String aiSummary, LocalDate expiredAt,
                  Long watchCnt, Long quitCnt, UploadStatusType uploadStatus) {
        this.organization = organization;
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.videoKey = videoKey;
        this.thumbnailKey = thumbnailKey;
        this.hlsPrefix = hlsPrefix;
        this.wholeTime = wholeTime;
        this.isComment = isComment;
        this.aiFunctionType = aiFunctionType;
        this.aiFeedback = aiFeedback;
        this.aiSummary = aiSummary;
        this.expiredAt = expiredAt;
        this.watchCnt = watchCnt;
        this.quitCnt = quitCnt;
        this.uploadStatus = uploadStatus;
    }

    public static Video create(Organization organization, Member creator, String title, String description,
                               String videoKey, String thumbnailKey, String hlsPrefix, Long wholeTime,
                               boolean isComment, AiFunctionType aiFunctionType, LocalDate expiredAt) {
        if (expiredAt == null) {
            expiredAt = LocalDate.now().plusYears(100);
        }

        return Video.builder()
                .organization(organization)
                .creator(creator)
                .title(title)
                .description(description)
                .videoKey(videoKey)
                .thumbnailKey(thumbnailKey)
                .hlsPrefix(hlsPrefix)
                .wholeTime(wholeTime)
                .isComment(isComment)
                .aiFunctionType(aiFunctionType)
                .expiredAt(expiredAt)
                .watchCnt(0L)
                .quitCnt(0L)
                .uploadStatus(IN_PROGRESS)
                .build();
    }

    public void watch() {
        this.watchCnt++;
    }

    public void quit() {
        this.quitCnt++;
    }

    public void modify(String description, boolean isComment, LocalDate expiredAt) {
        if (expiredAt == null) {
            expiredAt = LocalDate.now().plusYears(100);
        }

        this.description = description;
        this.isComment = isComment;
        this.expiredAt = expiredAt;
    }
}
