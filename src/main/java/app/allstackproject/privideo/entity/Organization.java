package app.allstackproject.privideo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Organization")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organization extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id")
    private User creator;

    @NotBlank
    @Column(unique = true)
    private String name;

    @NotNull
    private String imgUrl;

    // TODO: 생성 시 기본 이미지 확정되면 @NotBlank로 복원 및 적용
    @NotNull
    private String adImgUrl;

    @NotBlank
    private String description;

    @Builder(access = AccessLevel.PRIVATE)
    private Organization(User creator, String name, String imgUrl, String adImgUrl, String description) {
        this.creator = creator;
        this.name = name;
        this.imgUrl = imgUrl;
        this.adImgUrl = adImgUrl;
        this.description = description;
    }

    public static Organization create(User creator, String name, String imgUrl, String description) {
        return Organization.builder()
                .creator(creator)
                .name(name)
                .imgUrl(imgUrl)
                .adImgUrl("")
                .description(description)
                .build();
    }

    public void modifyImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
