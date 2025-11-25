package app.allstackproject.privideo.repository.comment.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.common.enumStatus.JoinStatusType.APPROVED;
import static app.allstackproject.privideo.common.enumStatus.UploadStatusType.COMPLETE;
import static app.allstackproject.privideo.entity.QComment.comment;
import static app.allstackproject.privideo.entity.QMember.member;
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
                        member.nickname,
                        comment.createdAt,
                        comment.parentCommentId.isNotNull(),
                        comment.parentCommentId))
                .from(comment)
                .join(comment.video, video)
                .join(comment.member, member)
                .where(video.id.eq(videoId),
                        video.uploadStatus.eq(COMPLETE),
                        comment.member.status.eq(ACTIVE),
                        comment.member.joinStatus.eq(APPROVED))
                .orderBy(comment.createdAt.desc())
                .fetch();
    }
}
