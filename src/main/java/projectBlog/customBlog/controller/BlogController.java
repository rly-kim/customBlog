package projectBlog.customBlog.controller;

import java.time.LocalDateTime;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import projectBlog.customBlog.SessionConst;
import projectBlog.customBlog.domain.Article;
import projectBlog.customBlog.domain.Blog;
import projectBlog.customBlog.domain.Category;
import projectBlog.customBlog.domain.Member;
import projectBlog.customBlog.domain.Pagination;
import projectBlog.customBlog.dto.ArticleForm;
import projectBlog.customBlog.dto.CommentForm;
import projectBlog.customBlog.repository.ArticleRepository;
import projectBlog.customBlog.repository.BlogRepository;
import projectBlog.customBlog.repository.CategoryRepository;
import projectBlog.customBlog.service.ArticleService;
import projectBlog.customBlog.service.BlogService;
import projectBlog.customBlog.service.CategoryService;
import projectBlog.customBlog.service.CommentService;
import projectBlog.customBlog.service.MemberService;
import projectBlog.customBlog.validation.ArticleValidator;
import projectBlog.customBlog.validation.CategoryValidator;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BlogController {

    private final CategoryRepository categoryRepository;
    private final BlogService blogService;
    private final BlogRepository blogRepository;
    private final CommentService commentService;
    private final ArticleService articleService;
    private final ArticleRepository articleRepository;
    private final ArticleValidator articleValidator;

    @GetMapping("/blog/{blogId}")
    public String getBlog(@PathVariable("blogId") int blogId, @RequestParam(defaultValue = "1") int page, Model model) {

        Blog blog = blogRepository.findBlog(blogId);
        int totalListCnt = articleRepository.getAllArticles(blog).size(); // 총 게시물 수
        Pagination pagination = new Pagination(totalListCnt, page);
        int startIndex = pagination.getStartIndex();
        int pageSize = pagination.getPageSize();

        List<Article> articles = blogService.getPageArticlesOfBlog(blogId, startIndex, pageSize);
        List<Category> categories = categoryRepository.getAllCategoriesOfBlog(blogId);
        Member member = blog.getMember();

        model.addAttribute("articles", articles);
        model.addAttribute("pagination", pagination);
        model.addAttribute("categories", categories);
        model.addAttribute("blog", blog);
        model.addAttribute("member", member);
        return "bs_blog/index";
    }


    @GetMapping("/blog/write/{blogId}")
    public String getArticleForm(@PathVariable("blogId") int blogId, @ModelAttribute("articleForm") ArticleForm form, Model model) {
        List<Category> categories = categoryRepository.getAllCategoriesOfBlog(blogId);
        model.addAttribute("blogId", blogId);
        model.addAttribute("categories", categories);
        return "article/articleForm";
    }

    @GetMapping("/blog/{blogId}/article")
    public String getArticle(@PathVariable("blogId") int blogId, @ModelAttribute("commentForm") CommentForm commentForm, @RequestParam(defaultValue = "1") int articleId, Model model) {

        Blog blog = blogRepository.findBlog(blogId);
        Article article = articleRepository.findArticle(articleId);

        model.addAttribute("blog", blog);
        model.addAttribute("article", article);

        return "bs_blog/single";
    }


    @PostMapping("/blog/write/{blogId}")
    public String postArticle(@PathVariable("blogId") int blogId, @ModelAttribute("articleForm") ArticleForm form, BindingResult bindingResult) {

        articleValidator.validate(form, bindingResult);

        if(bindingResult.hasErrors()) {
            log.info("{}", bindingResult);
            return "article/articleForm";
        }

        Blog blog = blogRepository.findBlog(blogId);
        Member member = blog.getMember();
        Category category = categoryRepository.findCategory(form.getCategoryId());
        Article article = Article.makeArticle(form.getTitle(), form.getContent(), LocalDateTime.now(), blog, member, category);
        articleService.saveArticle(article);
        return "redirect:/blog/{blogId}";
    }

    @PostMapping("/comment/{blogId}/parent/{articleId}")
    public String postParentComment(@PathVariable("blogId") int blogId, @PathVariable("articleId") int articleId, @ModelAttribute("commentForm") CommentForm commentForm, HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        Member member = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
        String content = commentForm.getContent();
        commentService.postParentComment(articleId, content, member.getId());
        return "redirect:/blog/{blogId}"; /** */
    }

    @GetMapping("/blog/{blogId}/category/{categoryId}")
    public String getCategory(@PathVariable("blogId") int blogId, @PathVariable("categoryId") int categoryId, @RequestParam(defaultValue = "1") int page, Model model) {

        int totalListCnt = categoryRepository.getArticlesOfCategory(blogId, categoryId).size(); // 카테고리 총 게시물 수
        Pagination pagination = new Pagination(totalListCnt, page);
        int startIndex = pagination.getStartIndex();
        int pageSize = pagination.getPageSize();

        List<Article> articles = categoryRepository.getPageArticlesOfCategory(blogId, categoryId, startIndex, pageSize);
        List<Category> categories = categoryRepository.getAllCategoriesOfBlog(blogId);

        model.addAttribute("blogId", blogId);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("articles", articles);
        model.addAttribute("pagination", pagination);

        return "bs_blog/fashion";
    }


}
