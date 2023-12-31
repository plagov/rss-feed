package io.plagov.rssfeed.view;

import io.plagov.rssfeed.dao.PostDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PostsView {

    private final PostDao postDao;

    public PostsView(PostDao postDao) {
        this.postDao = postDao;
    }

    @GetMapping("/")
    public String getMainView(Model model) {
        var posts = postDao.getAllUnreadPosts();
        model.addAttribute("posts", posts);
        return "index";
    }

    @PostMapping("/mark-as-read")
    public String markPostAsRead(@RequestParam String id) {
        postDao.markPostAsRead(Integer.parseInt(id));
        return "redirect:/";
    }
}
