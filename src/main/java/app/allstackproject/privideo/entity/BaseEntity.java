package app.allstackproject.privideo.entity;

import app.allstackproject.privideo.common.enumStatus.BaseStatusType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime lastModifiedAt;

    @Column(name = "status", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private BaseStatusType status;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastModifiedAt = LocalDateTime.now();
        status = BaseStatusType.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedAt = LocalDateTime.now();
    }

    public void updateToActive() {
        this.status = BaseStatusType.ACTIVE;
    }

    public void updateToInactive() {
        this.status = BaseStatusType.INACTIVE;
    }

    public boolean isActive() {
        return this.status == BaseStatusType.ACTIVE;
    }
}
