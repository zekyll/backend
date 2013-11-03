package rage.codebrowser.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import rage.codebrowser.dto.Tag;
import rage.codebrowser.dto.TagCategory;
import rage.codebrowser.dto.TagName;
import rage.codebrowser.repository.TagCategoryRepository;
import rage.codebrowser.repository.TagNameRepository;

@Controller
public class TagRESTController {

    @Autowired
    private TagNameRepository tagNameRepository;
    @Autowired
    private TagCategoryRepository tagCategoryRepository;
    

    @RequestMapping(value = {"tagnames"})
    @ResponseBody
    public List<TagName> getTagNames() {
        return tagNameRepository.findAll(new Sort(Sort.Direction.ASC, "name"));
    }

    @RequestMapping(value = {"tagnames/exerciseanswers"})
    @ResponseBody
    public List<TagName> getExerciseAnswerTagNames() {
        return tagNameRepository.findExerciseAnswerTagNames();
    }

    @RequestMapping(value = {"tagnames/snapshots"})
    @ResponseBody
    public List<TagName> getSnapshotTagNames() {
        return tagNameRepository.findSnapshotTagNames();
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
    
    @RequestMapping(value = {"tagcategories"})
    @ResponseBody
    public List<TagCategory> getTagCategories() {
        return tagCategoryRepository.findAll(new Sort(Sort.Direction.ASC, "name"));
    }
    
    @RequestMapping(value = {"tagcategories/{tagCategoryId}"})
    @ResponseBody
    public TagCategory getTagCategory(@PathVariable("tagCategoryId") TagCategory tagCategory) {
        return tagCategory;
    }
        
    @RequestMapping(value = {"tagcategories/{tagCategoryId}/tagnames"})
    @ResponseBody
    public List<TagName> getTagCategoryTagNames(@PathVariable("tagCategoryId") TagCategory tagCategory) {
        return tagCategory.getTagnames();
    }
    
    @RequestMapping(value = {"tagcategories/{tagCategoryId}/tagnames/snapshots"})
    @ResponseBody
    public List<TagName> getTagCategoryTagNamesForSnapshots(@PathVariable("tagCategoryId") TagCategory tagCategory) {
        List<TagName> snapshotTagNames = tagNameRepository.findSnapshotTagNames();
        List<TagName> categoryTags = tagCategory.getTagnames();
        List<TagName> categorySnapshotTags = new ArrayList();
        for (TagName tagName : categoryTags) {
            if (snapshotTagNames.contains(tagName)) {
                categorySnapshotTags.add(tagName);
            }
        }
        return categorySnapshotTags;
    }
    
    @RequestMapping(value = {"tagcategories/{tagCategoryId}/tagnames/exercises"})
    @ResponseBody
    public List<TagName> getTagCategoryTagNamesForExercises(@PathVariable("tagCategoryId") TagCategory tagCategory) {
        List<TagName> exerciseTagNames = tagNameRepository.findExerciseAnswerTagNames();
        List<TagName> categoryTags = tagCategory.getTagnames();
        List<TagName> categorySnapshotTags = new ArrayList();
        for (TagName tagName : categoryTags) {
            if (exerciseTagNames.contains(tagName)) {
                categorySnapshotTags.add(tagName);
            }
        }
        return categorySnapshotTags;
    }
    
    @RequestMapping(value = {"tagcategories/{tagCategoryId}/tagnames/unused"})
    @ResponseBody
    public List<TagName> getUnusedTagCategoryTagNames(@PathVariable("tagCategoryId") TagCategory tagCategory) {
        List<TagName> allTagNames = tagNameRepository.findAll();
        List<TagName> categoryTags = tagCategory.getTagnames();
        List<TagName> unusedTags = new ArrayList();
        for (TagName tagName : allTagNames) {
            if (!categoryTags.contains(tagName)) {
                unusedTags.add(tagName);
            }
        }
        return unusedTags;
    }
    
    @RequestMapping(value = {"tagcategories/{tagCategoryId}"}, method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Transactional
    public TagCategory postTagCategoryTagNames(@RequestBody TagCategory tagcategory) {
        TagCategory existing = tagCategoryRepository.findByName(tagcategory.getName());
        for (TagName tagname : tagcategory.getTagnames()) {
            existing.addTagName(tagname);
        }
        tagCategoryRepository.saveAndFlush(existing);
        return existing;
    }
        
    @RequestMapping(value = {"tagcategories"}, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public TagCategory postTagategory(@RequestBody TagCategory tagCategory) {
        TagCategory existing = tagCategoryRepository.findByName(tagCategory.getName());
        if (existing != null) {
            return existing;
        }
        return tagCategoryRepository.saveAndFlush(tagCategory);
    }
}
