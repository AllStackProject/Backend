package app.allstackproject.privideo.domain.comment;

import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.COMMENT_NOT_FOUND;
import static app.allstackproject.privideo.global.response.status.BaseExceptionResponseStatus.COMMENT_UNAUTHORIZED_DELETE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import app.allstackproject.privideo.domain.comment.entity.Comment;
import app.allstackproject.privideo.domain.comment.repository.CommentRepository;
import app.allstackproject.privideo.domain.comment.service.CommentService;
import app.allstackproject.privideo.domain.member.entity.Member;
import app.allstackproject.privideo.domain.member.repository.MemberRepository;
import app.allstackproject.privideo.domain.video.repository.VideoRepository;
import app.allstackproject.privideo.global.exception.ApiException;
import app.allstackproject.privideo.global.util.CdnUrlProvider;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private CdnUrlProvider cdnUrlProvider;

    @Test
    @DisplayName("존재하지 않는 댓글이면 COMMENT_NOT_FOUND 예외")
    void deleteComment_notFound() {
        Long memberId = 1L;
        Long orgId = 10L;
        Long commentId = 100L;

        given(commentRepository.findById(commentId))
                .willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> commentService.deleteComment(memberId, orgId, commentId));

        assertThat(ex.getResponseStatus()).isEqualTo(COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 사용자의 댓글 삭제 시 COMMENT_UNAUTHORIZED_DELETE 예외")
    void deleteComment_unauthorized() {
        Long memberId = 1L;
        Long orgId = 10L;
        Long commentId = 100L;

        Comment comment = org.mockito.Mockito.mock(Comment.class);
        Member owner = org.mockito.Mockito.mock(Member.class);

        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(comment.getMember()).willReturn(owner);
        given(owner.getId()).willReturn(999L); // 다른 사람

        ApiException ex = assertThrows(ApiException.class,
                () -> commentService.deleteComment(memberId, orgId, commentId));

        assertThat(ex.getResponseStatus()).isEqualTo(COMMENT_UNAUTHORIZED_DELETE);
    }

    @Test
    @DisplayName("본인 댓글 삭제 성공 시 자식 댓글 + 본인 댓글 삭제")
    void deleteComment_success() {
        Long memberId = 1L;
        Long orgId = 10L;
        Long commentId = 100L;

        Comment comment = org.mockito.Mockito.mock(Comment.class);
        Member owner = org.mockito.Mockito.mock(Member.class);

        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(comment.getMember()).willReturn(owner);
        given(owner.getId()).willReturn(memberId);

        boolean result = commentService.deleteComment(memberId, orgId, commentId);

        assertThat(result).isTrue();
        verify(commentRepository).deleteAllByParentCommentId(commentId);
        verify(commentRepository).deleteById(commentId);
    }
}
