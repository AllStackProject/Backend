package app.allstackproject.privideo.repository.comment.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.entity.QComment.comment;
import static app.allstackproject.privideo.entity.QMember.member;
import static app.allstackproject.privideo.entity.QUser.user;
import static app.allstackproject.privideo.entity.QVideo.video;

import app.allstackproject.privideo.dto.video.CommentInfo;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CommentInfo> findAllByVideoId(Long videoId) {
        return jpaQueryFactory
                .select(Projections.constructor(CommentInfo.class,
                        comment.id,
                        comment.text,
                        user.name,
                        comment.createdAt,
                        comment.parentCommentId.isNotNull(),
                        comment.parentCommentId))
                .from(comment)
                .join(comment.video, video)
                .join(comment.member, member)
                .join(member.user, user)
                .where(video.id.eq(videoId),
                        comment.member.status.eq(ACTIVE),
                        comment.member.joinStatus.eq(APPROVED),
                        comment.member.user.status.eq(ACTIVE))
                .orderBy(comment.createdAt.desc())
                .fetch();
    }
}
