package app.allstackproject.privideo.dto.user;

import app.allstackproject.privideo.common.enumStatus.GenderType;
import app.allstackproject.privideo.entity.Member;
import app.allstackproject.privideo.entity.User;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String name;
    private String email;
    private GenderType gender;
    private int ages;
    private String phoneNumber;
    private List<String> organizations;

    public static UserInfoResponse of(User user, List<Member> members) {
        List<String> organization = members.stream()
                .map(member -> member.getOrganization().getName())
                .collect(Collectors.toList());

        return UserInfoResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .gender(user.getGender())
                .ages(user.getAge())
                .phoneNumber(user.getPhoneNumber())
                .organizations(organization)
                .build();
    }
}
