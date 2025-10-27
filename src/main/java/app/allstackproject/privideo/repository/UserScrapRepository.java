package app.allstackproject.privideo.repository;

import app.allstackproject.privideo.entity.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserScrapRepository extends JpaRepository<Scrap, Long> {
    List<Scrap> findByMemberId(Long MemberId);
}
