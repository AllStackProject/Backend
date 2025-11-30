package app.allstackproject.privideo.domain.notice.repository;

import app.allstackproject.privideo.domain.notice.entity.Notice;
import app.allstackproject.privideo.domain.notice.repository.custom.NoticeRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeRepositoryCustom {
    Optional<Notice> findByIdAndOrganizationId(Long id, Long orgId);
}
