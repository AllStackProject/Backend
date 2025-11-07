package app.allstackproject.privideo.repository;

import app.allstackproject.privideo.entity.OrgViewLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrgViewLogRepository extends MongoRepository<OrgViewLog, Long> {
}
