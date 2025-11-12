package app.allstackproject.privideo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String title;

    private Long memberGroupId;

    @Builder(access = AccessLevel.PRIVATE)
    private Category(String title, Long memberGroupId) {
        this.title = title;
        this.memberGroupId = memberGroupId;
    }

    public static Category create(String title, Long memberGroupId) {
        return Category.builder()
                .title(title)
                .memberGroupId(memberGroupId)
                .build();
    }

    public void modifyTitle(String newTitle) {
        this.title = newTitle;
    }
}
