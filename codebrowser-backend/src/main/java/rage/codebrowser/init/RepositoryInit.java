
package rage.codebrowser.init;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RepositoryInit {

    @Autowired
    RepositoryInitService service;
    
    @PostConstruct
    @Transactional
    public void init() {
        service.initRepository();
    }

}
