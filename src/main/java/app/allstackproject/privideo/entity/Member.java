package app.allstackproject.privideo.entity;

import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.REJECTED;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ALREADY_APPROVED_MEMBER;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ALREADY_REJECTED_MEMBER;

import app.allstackproject.privideo.common.enumStatus.JoinStatusType;
import app.allstackproject.privideo.common.enumStatus.PermissionType;
import app.allstackproject.privideo.common.exception.ApiException;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
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

    @Enumerated(EnumType.STRING)
    private JoinStatusType joinStatus;

    private long permissionCode = 0L;

    // TODO: 낙관적 락
    @Version
    private Long version;

    @Builder(access = AccessLevel.PRIVATE)
    private Member(User user, Organization organization, boolean isAdmin, JoinStatusType joinStatus,
                   Long permissionCode) {
        this.user = user;
        this.organization = organization;
        this.joinStatus = joinStatus;
        this.isAdmin = isAdmin;
        if (permissionCode != null) {
            this.permissionCode = permissionCode;
        }
    }

    public static Member create(User user, Organization organization, boolean isAdmin, JoinStatusType joinStatus) {
        return Member.builder()
                .user(user)
                .organization(organization)
                .isAdmin(isAdmin)
                .joinStatus(joinStatus)
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

    public void changeJoinStatus(JoinStatusType destStatus) {
        switch (joinStatus) {
            case APPROVED -> {
                if (destStatus.equals(APPROVED)) {
                    throw new ApiException(ALREADY_APPROVED_MEMBER);
                }
            }
            case REJECTED -> {
                if (destStatus.equals(REJECTED)) {
                    throw new ApiException(ALREADY_REJECTED_MEMBER);
                }
            }
        }
        
        this.joinStatus = destStatus;
    }
}
