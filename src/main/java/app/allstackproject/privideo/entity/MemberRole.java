package app.allstackproject.privideo.entity;

import app.allstackproject.privideo.common.enumStatus.PermissionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Member_Role")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRole extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    private long code;

    @Builder(access = AccessLevel.PRIVATE)
    private MemberRole(Member member, long code) {
        this.member = member;
        this.code = code;
    }

    public static MemberRole create(Member member, long code) {
        return MemberRole.builder()
                .member(member)
                .code(code)
                .build();
    }

    /**
     * 권한 추가 (비트 OR)
     */
    public void grant(PermissionType... perms) {
        for (PermissionType p : perms) {
            code |= p.getBit();
        }
    }

    void _setMember(Member member) {
        this.member = member;
    }

    /**
     * 권한 회수 (비트 AND-NOT)
     */
    public void revoke(PermissionType... perms) {
        for (PermissionType p : perms) {
            code &= ~p.getBit();
        }
    }

    /**
     * 특정 권한이 있는지 확인
     */
    public boolean has(PermissionType p) {
        return (code & p.getBit()) == p.getBit();
    }
}
