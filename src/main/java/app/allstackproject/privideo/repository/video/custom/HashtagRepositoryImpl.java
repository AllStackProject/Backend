package app.allstackproject.privideo.repository.video.custom;

import static app.allstackproject.privideo.entity.QHashtag.hashtag;
import static app.allstackproject.privideo.entity.QVideoHashtagMapping.videoHashtagMapping;

import app.allstackproject.privideo.common.enumStatus.BaseStatusType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HashtagRepositoryImpl implements HashtagRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<String> findAllByVideoId(Long videoId) {
        return jpaQueryFactory
                .select(hashtag.title)
                .from(videoHashtagMapping)
                .join(videoHashtagMapping.hashtag, hashtag)
                .where(videoHashtagMapping.video.id.eq(videoId)
                        .and(videoHashtagMapping.status.eq(BaseStatusType.ACTIVE))
                        .and(hashtag.status.eq(BaseStatusType.ACTIVE)))
                .orderBy(hashtag.title.asc())
                .fetch();
    }
}
