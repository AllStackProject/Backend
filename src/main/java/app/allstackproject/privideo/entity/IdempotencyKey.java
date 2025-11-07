package app.allstackproject.privideo.entity;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("session_idempotency")
@CompoundIndexes({
        @CompoundIndex(name = "ux_session_type", def = "{'sessionId':1,'type':1}", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class IdempotencyKey {
    @Id
    private String id;

    @NotNull
    private String sessionId;

    @NotNull
    private String type;      // "JOIN" | "FLUSH"

    @NotNull
    private Instant createdAt;

    @NotNull
    @Indexed(name = "ttl_expireAt", expireAfter = "0s")
    private Instant expiredAt;
}
