package app.allstackproject.privideo.repository.history;

import app.allstackproject.privideo.common.enumStatus.BaseStatusType;
import app.allstackproject.privideo.entity.History;
import app.allstackproject.privideo.repository.history.custom.HistoryRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long>, HistoryRepositoryCustom {
    Optional<History> findByMemberIdAndVideoId(Long memberId, Long videoId);
}
