package app.allstackproject.privideo.domain.organization.repository;

import app.allstackproject.privideo.domain.organization.entity.Organization;
import app.allstackproject.privideo.domain.organization.repository.custom.OrganizationRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>, OrganizationRepositoryCustom {
    Optional<Organization> findByName(String name);
}
