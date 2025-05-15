package io.plagov.rssfeed.view;

import io.plagov.rssfeed.service.BlogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class BlogsView {

    private final BlogService blogService;

    public BlogsView(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/blogs")
    public String getBlogs(Model model) {
        var blogs = blogService.getSubscribedBlogs();
        model.addAttribute("blogs", blogs);
        return "blogs";
    }

    @PostMapping("/blogs/{id}/unsubscribe")
    public String unsubscribeFromBlog(@PathVariable int id) {
        blogService.unsubscribeFromBlog(id);
        return "redirect:/blogs";
    }

    @GetMapping("/blogs/subscribe")
    public ModelAndView getSubscriptionForm() {
        return new ModelAndView("subscribe");
    }

    @PostMapping("/blogs")
    public String subscribeToNewBlog(@RequestParam String feedUrl, @RequestParam String name) {
        blogService.subscribeToNewBlog(feedUrl, name);
        return "redirect:/blogs";
    }
}
