package app.allstackproject.privideo.repository;

import app.allstackproject.privideo.entity.OrgViewLog;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrgViewLogRepository extends MongoRepository<OrgViewLog, Long> {
    List<OrgViewLog> findByOrgIdAndDateBetween(Long orgId, LocalDate startDate, LocalDate endDate);
}
