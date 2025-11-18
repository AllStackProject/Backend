package app.allstackproject.privideo.repository.video.custom;

import static app.allstackproject.privideo.common.enumStatus.BaseStatusType.ACTIVE;
import static app.allstackproject.privideo.entity.QCategory.category;
import static app.allstackproject.privideo.entity.QVideoCategoryMapping.videoCategoryMapping;

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
                .where(
                        videoCategoryMapping.video.id.eq(videoId),
                        videoCategoryMapping.status.eq(ACTIVE),
                        category.status.eq(ACTIVE)
                )
                .orderBy(category.title.asc())
                .fetch();
    }
}
