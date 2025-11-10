package app.allstackproject.privideo.repository.video.custom;

import static app.allstackproject.privideo.entity.QCategory.category;
import static app.allstackproject.privideo.entity.QVideoCategoryMapping.videoCategoryMapping;

import app.allstackproject.privideo.common.enumStatus.BaseStatusType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<String> findAllByVideoId(Long videoId) {
        return jpaQueryFactory
                .select(category.title)
                .from(videoCategoryMapping)
                .join(videoCategoryMapping.category, category)
                .where(videoCategoryMapping.video.id.eq(videoId)
                        .and(videoCategoryMapping.status.eq(BaseStatusType.ACTIVE))
                        .and(category.status.eq(BaseStatusType.ACTIVE)))
                .orderBy(category.title.asc())
                .fetch();
    }
}
