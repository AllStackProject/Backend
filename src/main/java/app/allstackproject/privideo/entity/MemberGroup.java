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
@Table(name = "Member_Group")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberGroup extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @NotBlank
    private String name;

    @Builder(access = AccessLevel.PRIVATE)
    private MemberGroup(Organization organization, String name) {
        this.organization = organization;
        this.name = name;
    }

    public MemberGroup create(Organization organization, String name) {
        return MemberGroup.builder()
                .organization(organization)
                .name(name)
                .build();
    }
}
