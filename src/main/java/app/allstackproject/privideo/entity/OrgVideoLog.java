package app.allstackproject.privideo.entity;

import jakarta.persistence.Id;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("org_video_log")
@CompoundIndexes({
        @CompoundIndex(name = "ux_org_date", def = "{'orgId':1,'date':1}", unique = false)
})
@Getter
@Setter
@NoArgsConstructor
public class OrgVideoLog {
    @Id
    private String id;          // "org:{orgId}|{yyyy-MM-dd}"

    private long orgId;

    private String date;

    private Map<String, Integer> buckets = new LinkedHashMap<>(Map.of(
            "00-03", 0, "03-06", 0, "06-09", 0, "09-12", 0,
            "12-15", 0, "15-18", 0, "18-21", 0, "21-24", 0
    ));

    private Instant updatedAt;
}
