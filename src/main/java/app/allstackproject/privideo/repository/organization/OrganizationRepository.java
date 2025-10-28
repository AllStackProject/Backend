package app.allstackproject.privideo.repository.organization;

import app.allstackproject.privideo.entity.Organization;
import app.allstackproject.privideo.repository.organization.custom.OrganizationRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>, OrganizationRepositoryCustom {
    Optional<Organization> findByCode(String code);

    Optional<Organization> findByName(String name);

    Optional<Organization> findByNameAndCode(String name, String code);
}
