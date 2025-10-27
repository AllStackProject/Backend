package app.allstackproject.privideo.dto.user;

import app.allstackproject.privideo.common.annotation.PasswordConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchPasswordRequest {
    @PasswordConstraint
    private String currentPassword;

    @PasswordConstraint
    private String newPassword;
}
