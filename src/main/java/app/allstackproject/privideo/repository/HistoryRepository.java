package app.allstackproject.privideo.repository;

import app.allstackproject.privideo.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    List<History> findByMemberIdAndVideoOrganizationId(Long memberId, Long orgId);
}
