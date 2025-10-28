package app.allstackproject.privideo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank
    private String imgUrl;

    @NotBlank
    private String adImgUrl;

    @NotBlank
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    // TODO: 추후 Redis로 이관 예정
    @NotBlank
    private String code;

    @Builder(access = AccessLevel.PRIVATE)
    private Organization(User creator, String name, String imgUrl, String adImgUrl, String description, String code) {
        this.creator = creator;
        this.name = name;
        this.adImgUrl = adImgUrl;
        this.imgUrl = imgUrl;
        this.description = description;
        this.code = code;
    }

    public static Organization create(User creator, String name, String adImgUrl, String imgUrl, String description,
                                      String code) {
        return Organization.builder()
                .creator(creator)
                .name(name)
                .imgUrl(imgUrl)
                .adImgUrl(adImgUrl)
                .description(description)
                .code(code)
                .build();
    }
}
