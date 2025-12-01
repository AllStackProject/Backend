package app.allstackproject.privideo.domain.history.repository;

import app.allstackproject.privideo.domain.history.entity.History;
import app.allstackproject.privideo.domain.history.repository.custom.HistoryRepositoryCustom;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long>, HistoryRepositoryCustom {
    Optional<History> findByMemberIdAndVideoId(Long memberId, Long videoId);

    Long countByMemberIdAndIsCompleteIsTrueAndCompletedAtBetween(Long memberId, LocalDateTime startDate,
                                                                 LocalDateTime endDate);

    void deleteAllByVideoId(Long videoId);
}
