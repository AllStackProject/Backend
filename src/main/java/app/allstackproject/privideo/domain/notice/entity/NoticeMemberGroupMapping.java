package app.allstackproject.privideo.domain.notice.entity;

import app.allstackproject.privideo.domain.member.entity.MemberGroup;
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
@Table(name = "Notice_Member_Group_Mapping")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeMemberGroupMapping {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_group_id")
    private MemberGroup memberGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    @Builder(access = AccessLevel.PRIVATE)
    private NoticeMemberGroupMapping(MemberGroup memberGroup, Notice notice) {
        this.memberGroup = memberGroup;
        this.notice = notice;
    }

    public static NoticeMemberGroupMapping create(MemberGroup memberGroup, Notice notice) {
        return NoticeMemberGroupMapping.builder()
                .memberGroup(memberGroup)
                .notice(notice)
                .build();
    }
}
