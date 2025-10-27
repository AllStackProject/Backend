package app.allstackproject.privideo.entity;

import app.allstackproject.privideo.common.enumStatus.PermissionType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "member",
        uniqueConstraints = @UniqueConstraint(name = "uk_member", columnNames = {"user_id", "organization_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    private boolean isAdmin;

    private long permissionCode = 0L;

    // TODO: 낙관적 락
    @Version
    private Long version;

    @Builder(access = AccessLevel.PRIVATE)
    private Member(User user, Organization organization, boolean isAdmin, Long permissionCode) {
        this.user = user;
        this.organization = organization;
        this.isAdmin = isAdmin;
        if (permissionCode != null) {
            this.permissionCode = permissionCode;
        }
    }

    public static Member create(User user, Organization organization, boolean isAdmin) {
        return Member.builder()
                .user(user)
                .organization(organization)
                .isAdmin(isAdmin)
                .permissionCode(0L)
                .build();
    }

    public void grant(PermissionType... perms) {
        for (PermissionType p : perms) {
            permissionCode |= p.getBit();
        }
    }

    public void revoke(PermissionType... perms) {
        for (PermissionType p : perms) {
            permissionCode &= ~p.getBit();
        }
    }

    public boolean has(PermissionType p) {
        return (permissionCode & p.getBit()) == p.getBit();
    }

    public void replaceWith(PermissionType... perms) {
        this.permissionCode = PermissionType.combine(perms);
    }
}
