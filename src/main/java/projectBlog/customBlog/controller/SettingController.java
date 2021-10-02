package projectBlog.customBlog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import projectBlog.customBlog.domain.Blog;
import projectBlog.customBlog.domain.Category;
import projectBlog.customBlog.repository.BlogRepository;
import projectBlog.customBlog.repository.CategoryRepository;
import projectBlog.customBlog.service.CategoryService;
import projectBlog.customBlog.validation.CategoryValidator;

@Controller
@Slf4j
@RequiredArgsConstructor
public class SettingController {

    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final CategoryValidator categoryValidator;
    private final BlogRepository blogRepository;

    @GetMapping("/blog/setting/{blogId}")
    public String getSetting(@PathVariable("blogId") int blogId, Model model) {

        Blog blog = blogRepository.findBlog(blogId);
        model.addAttribute("categories", blog.getCategories());
        model.addAttribute("blog", blog);
        return "blog/settingPage";

    }

    /**
     블로그 타이틀 수정
     */
    @PostMapping("/blog/edit/{blogId}")
    public String editBlogName(@PathVariable("blogId") int blogId, String blogName) {

        Blog blog = blogRepository.findBlog(blogId);
        blog.changeBlogName(blogName);
        return "redirect:/blog/settingPage";

    }

    /**
     카테고리 추가
     */
    @PostMapping("/blog/category/add/{blogId}")
    public String addCategory(@PathVariable("blogId") int blogId, String categoryName) {

        Blog blog = blogRepository.findBlog(blogId);
        Category category = Category.makeParentCategory(categoryName, blog);
        categoryService.saveCategory(category);
        return "redirect:/blog/settingPage";

    }

    /**
     카테고리 수정
     */
    @PostMapping("/blog/edit/{blogId}/{categoryId}")
    public String editCategory(@PathVariable("blogId") int blogId, @PathVariable("categoryId") int categoryid
    , String changedName) {

        categoryService.updateCategory(categoryid, changedName);
        return "redirect:/blog/settingPage";

    }


    /**
     카테고리 삭제
     */
    @PostMapping("/blog/delete/{blogId}/{categoryId}")
    public String deleteCategory(@PathVariable("categoryId") int categoryId, BindingResult bindingResult) {

        Category category = categoryRepository.findCategory(categoryId);

        categoryValidator.validate(category, bindingResult);

        if(bindingResult.hasGlobalErrors()) {
            log.info("{}", bindingResult);
            return "blog/settingPage";
        }

        categoryService.categoryDelete(categoryId);

        return "redirect:/blog/settingPage";
    }

}
