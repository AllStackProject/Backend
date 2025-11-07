package app.allstackproject.privideo.repository;

import app.allstackproject.privideo.entity.Scrap;
import app.allstackproject.privideo.repository.scrap.ScrapRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long>, ScrapRepositoryCustom {
    List<Scrap> findByMemberIdAndVideoOrganizationId(Long memberId, Long orgId);

    boolean existsByMemberIdAndVideoId(Long memberId, Long videoId);

    long deleteByMember_IdAndVideo_Id(Long memberId, Long videoId);
}
