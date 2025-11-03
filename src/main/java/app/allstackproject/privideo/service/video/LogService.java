package app.allstackproject.privideo.service.video;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogService {
    private static final int PACK_SIZE = 100;
    private final MongoTemplate mongoTemplate;

    public void incOrgViewBucket(long orgId, Instant nowUtc) {
        ZonedDateTime kst = nowUtc.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        String dateKey = kst.toLocalDate().toString();
        String bucketKey = to3hBucketKey(kst.getHour());

        String id = "org:%d|%s".formatted(orgId, dateKey);
        Query q = Query.query(Criteria.where("_id").is(id));

        Update u = new Update()
                .inc("buckets." + bucketKey, 1)
                .setOnInsert("orgId", orgId)
                .setOnInsert("date", dateKey)
                .currentDate("updatedAt");

        mongoTemplate.upsert(q, u, "org_view_log");
    }

    public void incVideoSegment(Long videoId, double positionSec) {
        int segIdx = (int) Math.floor(positionSec / 10.0);
        if (segIdx < 0) {
            segIdx = 0;
        }

        int packId = segIdx / PACK_SIZE;
        int slot = segIdx % PACK_SIZE;

        Query q = Query.query(Criteria.where("videoId").is(videoId).and("packId").is(packId));

        // 최초 업서트 시 counts 길이 100으로 채워두기
        List<Long> zeros = new ArrayList<>(PACK_SIZE);
        for (int i = 0; i < PACK_SIZE; i++) {
            zeros.add(0L);
        }

        Update u = new Update()
                .inc("counts." + slot, 1)
                .setOnInsert("videoId", videoId)
                .setOnInsert("packId", (long) packId)
                .setOnInsert("counts", zeros)
                .currentDate("updatedAt");

        mongoTemplate.upsert(q, u, "seg_view_log"); // 컬렉션명 또는 SegViewLogs.class
    }

    private String to3hBucketKey(int hour) {
        int start = (hour / 3) * 3;
        int end = start + 3;
        return "%02d-%02d".formatted(start, end);
    }

}
