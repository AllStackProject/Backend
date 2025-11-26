package app.allstackproject.privideo.repository.scrap;

import app.allstackproject.privideo.entity.Scrap;
import app.allstackproject.privideo.repository.scrap.custom.ScrapRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long>, ScrapRepositoryCustom {
    boolean existsByMemberIdAndVideoId(Long memberId, Long videoId);

    void deleteAllByVideoId(Long videoId);
}
