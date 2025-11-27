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
import lombok.Setter;

@Entity
@Getter
@Table(name = "Organization")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organization extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private User creator;

    @NotBlank
    @Column(unique = true)
    private String name;

    @NotNull
    @Setter
    @Column(name = "img_url")
    private String imgKey;

    @NotBlank
    private String description;

    @Builder(access = AccessLevel.PRIVATE)
    private Organization(User creator, String name, String imgKey, String description) {
        this.creator = creator;
        this.name = name;
        this.imgKey = imgKey;
        this.description = description;
    }

    public static Organization create(User creator, String name, String description) {
        return Organization.builder()
                .creator(creator)
                .name(name)
                .imgKey("")
                .description(description)
                .build();
    }
}
