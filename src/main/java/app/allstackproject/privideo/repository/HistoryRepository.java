package app.allstackproject.privideo.repository;

import app.allstackproject.privideo.common.enumStatus.BaseStatusType;
import app.allstackproject.privideo.entity.History;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    List<History> findByMemberIdAndVideoOrganizationId(Long memberId, Long orgId);

    Optional<History> findByMemberIdAndVideoIdAndStatus(Long memberId, Long videoId, BaseStatusType status);
}
