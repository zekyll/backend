package rage.codebrowser.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import rage.codebrowser.dto.Tag;
import rage.codebrowser.dto.TagName;
import rage.codebrowser.repository.TagNameRepository;

@Controller
public class TagRESTController {

    @Autowired
    private TagNameRepository tagNameRepository;

    @RequestMapping(value = {"tagnames"})
    @ResponseBody
    public List<TagName> getTagNames() {
        return tagNameRepository.findAll();
    }

    @RequestMapping(value = {"tagnames/{tagNameId}"})
    @ResponseBody
    public TagName getTagName(@PathVariable("tagNameId") TagName tagName) {
        return tagNameRepository.saveAndFlush(tagName);
    }

    @RequestMapping(value = {"tagnames/{tagNameId}/tags"})
    @ResponseBody
    public List<Tag> getTagNameTags(@PathVariable("tagNameId") TagName tagName) {
        return tagName.getTags();
    }

    @RequestMapping(value = {"tagnames"}, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public TagName postTagName(@RequestBody TagName tagName) {
        TagName existing = tagNameRepository.findByName(tagName.getName());
        if (existing != null) {
            return existing;
        }
        return tagNameRepository.saveAndFlush(tagName);
    }
}
