package rage.codebrowser.dto;

import java.io.Serializable;
import javax.persistence.MappedSuperclass;
import org.springframework.data.jpa.domain.AbstractPersistable;

@MappedSuperclass
public class AbstractNamedPersistable extends AbstractPersistable<Long> implements Serializable {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
