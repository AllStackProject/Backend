package app.allstackproject.privideo.service.video;

import app.allstackproject.privideo.entity.OrgViewLog;
import app.allstackproject.privideo.entity.SegQuitLogs;
import app.allstackproject.privideo.entity.SegViewLogs;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogService {
    private static final int PACK_SIZE = 100;
    public static final int SEGMENT_SECONDS = 10;

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
                .setOnInsert("date", kst.toLocalDate().atStartOfDay())
                .set("updatedAt", kst.toLocalDateTime().toString());

        mongoTemplate.upsert(q, u, OrgViewLog.class);
    }

    public void incSegViewBucket(Long videoId, BigInteger segments, int totalSegCnt) {
        if (segments == null || segments.signum() == 0 || totalSegCnt <= 0) {
            return;
        }

        Map<Integer, Map<Integer, Integer>> incByPack = new HashMap<>();

        BigInteger m = segments;
        while (m.signum() != 0) {
            int i = m.getLowestSetBit();
            m = m.clearBit(i);

            if (i >= totalSegCnt) {
                continue;
            }

            int packId = i / PACK_SIZE;
            int slot = i % PACK_SIZE;

            incByPack.computeIfAbsent(packId, k -> new HashMap<>())
                    .merge(slot, 1, Integer::sum);
        }

        if (incByPack.isEmpty()) {
            return;
        }

        Long[] zeros = new Long[PACK_SIZE];
        Arrays.fill(zeros, 0L);

        BulkOperations upsertBulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, SegViewLogs.class);
        BulkOperations incBulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, SegViewLogs.class);

        for (Entry<Integer, Map<Integer, Integer>> e : incByPack.entrySet()) {
            int packId = e.getKey();
            Map<Integer, Integer> slots = e.getValue();

            String id = "video:%d|pack:%d".formatted(videoId, packId);
            Query q = Query.query(Criteria.where("_id").is(id));

            Update init = new Update()
                    .setOnInsert("videoId", videoId)
                    .setOnInsert("packId", (long) packId)
                    .setOnInsert("counts", zeros)
                    .currentDate("updatedAt");
            upsertBulk.upsert(q, init);

            Update inc = new Update().currentDate("updatedAt");
            for (Entry<Integer, Integer> s : slots.entrySet()) {
                inc.inc("counts." + s.getKey(), s.getValue());
            }
            incBulk.updateOne(q, inc);
        }

        upsertBulk.execute();
        incBulk.execute();
    }

    public void incSegQuitBucket(Long videoId, Long recentPositionSec, int totalSegCnt) {
        if (videoId == null || recentPositionSec == null || totalSegCnt <= 0) {
            return;
        }

        long pos = Math.max(0L, recentPositionSec);
        int segIdxFromStart = (int) Math.min(pos / SEGMENT_SECONDS, (long) totalSegCnt - 1);

        int packId = segIdxFromStart / PACK_SIZE;
        int slot = segIdxFromStart % PACK_SIZE;

        String id = "video:%d|pack:%d".formatted(videoId, packId);
        Query q = Query.query(Criteria.where("_id").is(id));

        Long[] zeros = new Long[PACK_SIZE];
        Arrays.fill(zeros, 0L);

        Update init = new Update()
                .setOnInsert("videoId", videoId)
                .setOnInsert("packId", (long) packId)
                .setOnInsert("counts", zeros)
                .currentDate("updatedAt");
        mongoTemplate.upsert(q, init, SegQuitLogs.class);

        Update inc = new Update()
                .inc("counts." + slot, 1)
                .currentDate("updatedAt");
        mongoTemplate.updateFirst(q, inc, SegQuitLogs.class);
    }

    public List<Long> getSegViewCounts(Long videoId, int totalSegCnt) {
        if (videoId == null || totalSegCnt <= 0) {
            return List.of();
        }

        long[] result = new long[totalSegCnt];

        Query q = Query.query(Criteria.where("videoId").is(videoId));
        q.fields().include("packId").include("counts");

        List<SegViewLogs> packs = mongoTemplate.find(q, SegViewLogs.class);

        for (SegViewLogs pack : packs) {
            int base = Math.toIntExact(pack.getPackId()) * PACK_SIZE;
            Long[] counts = pack.getCounts();

            if (counts == null) {
                continue;
            }

            for (int i = 0; i < counts.length; i++) {
                int idx = base + i;
                if (idx >= totalSegCnt) {
                    break;
                }
                Long v = counts[i];
                if (v != null) {
                    result[idx] += v;
                }
            }
        }

        List<Long> out = new ArrayList<>(totalSegCnt);
        for (long v : result) {
            out.add(v);
        }
        return out;
    }


    private String to3hBucketKey(int hour) {
        int start = (hour / 3) * 3;
        int end = start + 3;
        return "%02d-%02d".formatted(start, end);
    }
}
