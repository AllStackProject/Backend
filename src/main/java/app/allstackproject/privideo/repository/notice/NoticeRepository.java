package app.allstackproject.privideo.repository.notice;

import app.allstackproject.privideo.entity.Notice;
import app.allstackproject.privideo.repository.notice.custom.NoticeRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeRepositoryCustom {
    Optional<Notice> findByIdAndOrganizationId(Long id, Long orgId);
}
