package app.allstackproject.privideo.entity;

import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.REJECTED;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ALREADY_APPROVED_MEMBER;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.ALREADY_REJECTED_MEMBER;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.FORBIDDEN_NO_PERMISSION;

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
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private String nickname;

    private boolean isAdmin;

    @Enumerated(EnumType.STRING)
    private JoinStatusType joinStatus;

    private long permissionCode = 0L;

//    // TODO: 낙관적 락
//    @Version
//    private Long version;

    @Builder(access = AccessLevel.PRIVATE)
    private Member(User user, Organization organization, String nickname, boolean isAdmin, JoinStatusType joinStatus,
                   Long permissionCode) {
        this.user = user;
        this.organization = organization;
        this.nickname = nickname;
        this.isAdmin = isAdmin;
        this.joinStatus = joinStatus;
        if (permissionCode != null) {
            this.permissionCode = permissionCode;
        }
    }

    public static Member create(User user, Organization organization, String nickname, boolean isAdmin,
                                JoinStatusType joinStatus) {
        return Member.builder()
                .user(user)
                .organization(organization)
                .nickname(nickname)
                .isAdmin(isAdmin)
                .joinStatus(joinStatus)
                .permissionCode(0L)
                .build();
    }

    //creator에게 모든 권한 부여
    public void adminPermissionSet() {
        if (!this.isAdmin) {
            throw new ApiException(FORBIDDEN_NO_PERMISSION);
        }

        this.permissionCode = PermissionType.combine(
                PermissionType.VIDEO_MANAGE,
                PermissionType.STATS_REPORT,
                PermissionType.NOTICE,
                PermissionType.ORG_SETTING
        );
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

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }
}
