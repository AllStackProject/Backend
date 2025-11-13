package app.allstackproject.privideo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member creator;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private Long watchCnt;

    @Builder(access = AccessLevel.PRIVATE)
    private Notification(Member creator, String title, String content, Long watchCnt) {
        this.creator = creator;
        this.title = title;
        this.content = content;
        this.watchCnt = watchCnt;
    }

    public static Notification create(Member creator, String title, String content, Long watchCnt) {
        return Notification.builder()
                .creator(creator)
                .title(title)
                .content(content)
                .watchCnt(watchCnt)
                .build();
    }
}
