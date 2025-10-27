package app.allstackproject.privideo.repository;

import app.allstackproject.privideo.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserHistoryRepository extends JpaRepository<History, Long> {
    List<History> findByMemberId(Long memberId);
}
