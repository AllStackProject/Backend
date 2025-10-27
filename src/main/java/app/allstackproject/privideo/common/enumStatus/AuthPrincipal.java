
package app.allstackproject.privideo.common.enumStatus;

public record AuthPrincipal(
        Long userId,
        Long memberId,
        Long orgId,
        Boolean orgIsCreator,
        Integer orgPermission,
        TokenType tokenType
) {
}