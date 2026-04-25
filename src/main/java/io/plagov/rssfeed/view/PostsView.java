package io.plagov.rssfeed.view;

import io.plagov.rssfeed.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PostsView {

    private final PostService postService;

    public PostsView(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/")
    public String getMainView(Model model) {
        var posts = postService.getUnreadPosts();
        model.addAttribute("posts", posts);
        return "index";
    }

    @PostMapping("/mark-as-read")
    public String markPostAsRead(@RequestParam String id) {
        postService.markPostAsRead(id);
        return "redirect:/";
    }
}
