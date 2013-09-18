package rage.codebrowser.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rage.codebrowser.dto.TagName;

public interface TagNameRepository extends JpaRepository<TagName, Long> {

    public TagName findByName(String name);
}
