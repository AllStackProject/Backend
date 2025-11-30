
package app.allstackproject.privideo.shared.enums;

public record AuthPrincipal(
        Long userId,
        Long memberId,
        Long orgId,
        Boolean orgIsAdmin,
        Long orgPermission,
        TokenType tokenType
) {
}