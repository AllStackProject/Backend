
package app.allstackproject.privideo.common.enumStatus;

public record AuthPrincipal(
        Long userId,
        Long memberId,
        Long orgId,
        Boolean orgIsAdmin,
        Integer orgPermission,
        TokenType tokenType
) {
}