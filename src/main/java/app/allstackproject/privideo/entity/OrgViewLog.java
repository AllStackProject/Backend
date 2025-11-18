package app.allstackproject.privideo.entity;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("org_view_log")
@CompoundIndexes({
        @CompoundIndex(name = "ux_org_date", def = "{'orgId':1,'date':1}", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class OrgViewLog {
    @Id
    private String id;          // "org:{orgId}|{yyyy-MM-dd}"

    @NotNull
    private Long orgId;

    @NotNull
    private LocalDateTime date;

    @NotNull
    private Map<String, Integer> buckets = new LinkedHashMap<>(Map.of(
            "00-03", 0, "03-06", 0, "06-09", 0, "09-12", 0,
            "12-15", 0, "15-18", 0, "18-21", 0, "21-24", 0
    ));

    @NotNull
    private String updatedAt;
}
