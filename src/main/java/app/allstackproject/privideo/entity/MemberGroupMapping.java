package app.allstackproject.privideo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "Member_Group_Mapping")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberGroupMapping extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_group_id")
    private MemberGroup memberGroup;

    @Builder(access = AccessLevel.PRIVATE)
    private MemberGroupMapping(Member member, MemberGroup memberGroup) {
        this.member = member;
        this.memberGroup = memberGroup;
    }

    public static MemberGroupMapping create(Member member, MemberGroup memberGroup) {
        return MemberGroupMapping.builder()
                .member(member)
                .memberGroup(memberGroup)
                .build();
    }
}
