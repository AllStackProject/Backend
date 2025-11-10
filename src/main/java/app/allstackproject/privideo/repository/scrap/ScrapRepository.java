package app.allstackproject.privideo.repository.scrap;

import app.allstackproject.privideo.entity.Scrap;
import app.allstackproject.privideo.repository.scrap.custom.ScrapRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long>, ScrapRepositoryCustom {
    List<Scrap> findByMemberIdAndVideoOrganizationId(Long memberId, Long orgId);

    boolean existsByMemberIdAndVideoId(Long memberId, Long videoId);
}
