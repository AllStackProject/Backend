package app.allstackproject.privideo.domain.video.entity;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("seg_view_log")
@CompoundIndexes({
        @CompoundIndex(name = "ux_video_pack", def = "{'videoId':1,'packId':1}", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class SegViewLogs {
    @Id
    private String id;          // "video:{videoId}|pack:{packId}"

    @NotNull
    private Long videoId;

    @NotNull
    private Long packId;

    @NotNull
    private Long[] counts;

    @NotNull
    private Instant updatedAt;
}
