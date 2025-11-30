package app.allstackproject.privideo.domain.video.repository.custom;

import static app.allstackproject.privideo.domain.video.entity.QCategory.category;
import static app.allstackproject.privideo.domain.video.entity.QVideoCategoryMapping.videoCategoryMapping;
import static app.allstackproject.privideo.domain.video.enums.UploadStatusType.COMPLETE;
import static app.allstackproject.privideo.shared.enums.BaseStatusType.ACTIVE;

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
                        videoCategoryMapping.video.uploadStatus.eq(COMPLETE),
                        category.status.eq(ACTIVE)
                )
                .orderBy(category.title.asc())
                .fetch();
    }
}
