package app.allstackproject.privideo.domain.scrap.repository;

import app.allstackproject.privideo.domain.scrap.entity.Scrap;
import app.allstackproject.privideo.domain.scrap.repository.custom.ScrapRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long>, ScrapRepositoryCustom {
    boolean existsByMemberIdAndVideoId(Long memberId, Long videoId);

    void deleteAllByVideoId(Long videoId);
}
