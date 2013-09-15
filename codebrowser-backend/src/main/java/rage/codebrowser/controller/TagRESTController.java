package rage.codebrowser.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rage.codebrowser.dto.Tag;
import rage.codebrowser.repository.TagRepository;

@Controller
public class TagRESTController {

    @Autowired
    private TagRepository tagRepository;

    @RequestMapping(value = {"tags"})
    @ResponseBody
    public List<Tag> getTags() {
        return tagRepository.findAll();
    }
}
