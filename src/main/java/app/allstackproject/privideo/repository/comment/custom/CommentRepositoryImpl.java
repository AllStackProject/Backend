package app.allstackproject.privideo.repository.comment.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.entity.QMember.member;
import static app.allstackproject.privideo.entity.QUser.user;
import static app.allstackproject.privideo.entity.QVideo.video;

import app.allstackproject.privideo.dto.video.CommentInfo;
import app.allstackproject.privideo.entity.QComment;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CommentInfo> findAllByVideoId(Long videoId) {
        QComment c = new QComment("c");

        return jpaQueryFactory
                .select(Projections.constructor(CommentInfo.class, c.id, c.text, user.name, c.createdAt,
                        c.parentCommentId.isNotNull(), c.parentCommentId))
                .from(c)
                .join(c.video, video)
                .join(c.member, member)
                .join(member.user, user)
                .where(video.id.eq(videoId)
                        .and(c.status.eq(ACTIVE)))
                .orderBy(c.createdAt.desc())
                .fetch();
    }
}
