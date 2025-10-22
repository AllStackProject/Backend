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
@Table(name = "Hashtag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hashtag extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String title;

    @Builder(access = AccessLevel.PRIVATE)
    private Hashtag(String title) {
        this.title = title;
    }

    public static Hashtag create(String title) {
        return Hashtag.builder()
                .title(title)
                .build();
    }
}
