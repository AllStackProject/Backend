package app.allstackproject.privideo.entity;

import app.allstackproject.privideo.common.enumStatus.PermissionType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;
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

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<MemberRole> memberRoles = new HashSet<>();

    private boolean isAdmin;

    @Builder(access = AccessLevel.PRIVATE)
    private Member(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public static Member create(boolean isAdmin) {
        return Member.builder()
                .isAdmin(isAdmin)
                .build();
    }

    public MemberRole addRole(long code) {
        MemberRole mr = MemberRole.create(this, code); // this 주입
        memberRoles.add(mr);
        return mr;
    }

    public void addRole(MemberRole mr) {
        memberRoles.add(mr);
        mr._setMember(this);            // 내부 세터로 양쪽 일치
    }

    public void removeRole(MemberRole mr) {
        memberRoles.remove(mr);
        mr._setMember(null);
    }

    /**
     * 멤버의 실효 권한 마스크 (모든 row OR)
     */
    public long effectiveMask() {
        long mask = 0L;
        for (MemberRole mr : memberRoles) {
            mask |= mr.getCode();
        }
        return mask;
    }

    /**
     * 멤버가 특정 권한을 갖는지
     */
    public boolean has(PermissionType p) {
        long bit = p.getBit();
        return (effectiveMask() & bit) == bit;
    }
}
